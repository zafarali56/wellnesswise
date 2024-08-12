package com.project.wellnesswise.data

import android.content.ContentValues.TAG
import android.nfc.Tag
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.androidgamesdk.gametextinput.Settings
import com.google.firebase.auth.FirebaseAuth
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

class HomeViewModel : ViewModel() {
    private val Tag = HomeViewModel::class.simpleName
    val isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val navigationItemList = listOf(
        NavigationItem(
            title = "Home",
            icon = Icons.Default.Home,
            description = "Home Screen",
            itemId = "Home Screen"
        ),
        NavigationItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            description = "Settings",
            itemId = "Settings screen"
        ),
        NavigationItem (
            title = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            description = "Logout",
            itemId = "Logout"
        )
    )

    fun checkForActiveSession ()
    {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.d(TAG, "Valid session")
            isUserLoggedIn.value = true
        }else {
           Log.d(TAG, "User is not logged in")
            isUserLoggedIn.value = false
        }
    }


}
