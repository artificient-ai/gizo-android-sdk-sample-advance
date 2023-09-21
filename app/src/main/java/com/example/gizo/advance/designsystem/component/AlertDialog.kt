package com.example.gizo.advance.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun GizoAlertDialog(
    modifier: Modifier = Modifier,
    isDismissNeeded: Boolean = true,
    isConfirmedNeeded: Boolean = true,
    title: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    confirmText: String = "",
    dismissText: String = "",
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {

    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.outlineVariant,
            onDismissRequest = {
                onDismiss()
                openDialog.value = false
            },
            title = {
                if (title != null) title()
            },
            text = {
              if (content != null) content()
            },
            confirmButton = {
               if (isConfirmedNeeded){
                   GizoTextButton(
                       onClick = {
                           onConfirm()
                           openDialog.value = false
                       }
                   ) {
                       Text(confirmText, style = MaterialTheme.typography.bodyMedium.copy(color= Color.White))
                   }
               }
            },
            dismissButton = {
                if (isDismissNeeded){
                    TextButton(
                        onClick = {
                            onDismiss()
                            openDialog.value = false
                        }
                    ) {
                        Text(dismissText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        )
    }
}