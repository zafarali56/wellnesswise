package com.project.wellnesswise.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteInterpreter(context: Context, modelName: String) {
    private val interpreter: Interpreter

    init {
        val tfliteModel: MappedByteBuffer = loadModelFile(context, modelName)
        interpreter = Interpreter(tfliteModel)
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    fun predict(input: List<Float>): List<Float> {
        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()

        Log.d("TFLiteInterpreter", "Input shape: ${inputShape.contentToString()}")
        Log.d("TFLiteInterpreter", "Output shape: ${outputShape.contentToString()}")
        Log.d("TFLiteInterpreter", "Input values: $input")

        if (input.size != inputShape[1]) {
            throw IllegalArgumentException("Input size (${input.size}) does not match model input shape (${inputShape[1]})")
        }

        val inputBuffer = ByteBuffer.allocateDirect(4 * inputShape[1])
            .order(ByteOrder.nativeOrder())
        inputBuffer.rewind()
        for (value in input) {
            inputBuffer.putFloat(value)
        }

        val outputBuffer = ByteBuffer.allocateDirect(4 * outputShape[1])
            .order(ByteOrder.nativeOrder())

        interpreter.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val output = List(outputShape[1]) { outputBuffer.float }
        Log.d("TFLiteInterpreter", "Output values: $output")
        return output
    }

    fun close() {
        interpreter.close()
    }
}