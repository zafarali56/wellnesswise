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
        Log.d("TFLiteInterpreter", "Model loaded: $modelName")
        Log.d("TFLiteInterpreter", "Input tensor shape: ${interpreter.getInputTensor(0).shape().contentToString()}")
        Log.d("TFLiteInterpreter", "Output tensor shape: ${interpreter.getOutputTensor(0).shape().contentToString()}")
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predict(input: FloatArray): FloatArray {
        Log.d("TFLiteInterpreter", "Input data: ${input.contentToString()}")

        val inputShape = intArrayOf(1, input.size)
        val inputBuffer = Array(1) { input }
        val outputShape = intArrayOf(1, 5)
        val outputBuffer = Array(1) { FloatArray(5) }

        interpreter.run(inputBuffer, outputBuffer)

        Log.d("TFLiteInterpreter", "Raw output: ${outputBuffer[0].contentToString()}")
        return outputBuffer[0]
    }

    fun close() {
        interpreter.close()
    }
}