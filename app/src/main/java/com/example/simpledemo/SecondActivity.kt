package com.example.simpledemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class SecondActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var text by remember { mutableStateOf("") }
            Column {
                TextField(
                    label = { Text("Enter text") },
                    value = text,
                    onValueChange = { text = it },
                )
                Button(onClick = {
                    val result = Intent()
                    val extras = Bundle()
                    extras.putString("text", text)
                    result.putExtras(extras)
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }) {
                    Text("Close")
                }
            }
        }
    }
}
