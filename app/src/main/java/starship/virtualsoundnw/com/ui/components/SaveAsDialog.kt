/*
 * Copyright (C) 2025 David L. Dawes
 * Notice: As this license may require, be aware this file is new and had been added by David L. Dawes since cloning the original archive from github.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package starship.virtualsoundnw.com.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

/**
 * Dialog for saving a ship with a new name
 */
@Composable
fun SaveAsDialog(
    currentShipName: String,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newName by remember { mutableStateOf(currentShipName) }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = { 
            Text(
                text = "Save Ship As",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a new name for this ship design:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Ship Name") },
                    isError = errorMessage != null,
                    supportingText = if (errorMessage != null) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(newName.trim()) },
                enabled = !isLoading && newName.trim().isNotBlank(),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(if (isLoading) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun SaveAsDialogPreview() {
    MyApplicationTheme {
        SaveAsDialog(
            currentShipName = "Enterprise",
            isLoading = false,
            errorMessage = null,
            onSave = {},
            onCancel = {}
        )
    }
}

@Preview
@Composable
private fun SaveAsDialogLoadingPreview() {
    MyApplicationTheme {
        SaveAsDialog(
            currentShipName = "Enterprise",
            isLoading = true,
            errorMessage = null,
            onSave = {},
            onCancel = {}
        )
    }
}

@Preview
@Composable
private fun SaveAsDialogErrorPreview() {
    MyApplicationTheme {
        SaveAsDialog(
            currentShipName = "Enterprise",
            isLoading = false,
            errorMessage = "A ship with this name already exists",
            onSave = {},
            onCancel = {}
        )
    }
}