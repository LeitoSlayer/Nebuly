package com.utadeo.nebuly.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.utadeo.nebuly.R

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(65.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.boton_volver),
            contentDescription = "Volver",
            modifier = Modifier.size(90.dp)
        )
    }
}
