package com.nfsp00fpro.app.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nfsp00fpro.app.R

/**
 * Custom vector drawable icons for the NFC PhreaK BoX application.
 * These are loaded from XML vector resources for better performance and consistency.
 */
object NfSp00fIcons {
    @Composable
    fun Dashboard(
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = Color.White
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dashboard),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun Nfc(
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = Color.White
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_nfc),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun Storage(
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = Color.White
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_storage),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun Security(
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = Color.White
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_security),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun Analytics(
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = Color.White
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_analytics),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun PhoneAndroid(
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = Color.White
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_phone_android),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}
