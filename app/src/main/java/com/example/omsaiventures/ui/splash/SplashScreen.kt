package com.example.omsaiventures.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omsaiventures.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.7f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val dividerWidth = remember { Animatable(0f) }
    val dotAlpha1 = remember { Animatable(0f) }
    val dotAlpha2 = remember { Animatable(0f) }
    val dotAlpha3 = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        launch { logoScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
        logoAlpha.animateTo(1f, animationSpec = tween(600))
        textAlpha.animateTo(1f, animationSpec = tween(500, delayMillis = 100))
        dividerWidth.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        taglineAlpha.animateTo(1f, animationSpec = tween(400))
        delay(300)
        dotAlpha1.animateTo(1f, tween(200)); delay(120)
        dotAlpha2.animateTo(1f, tween(200)); delay(120)
        dotAlpha3.animateTo(1f, tween(200))
        delay(900)
        onFinished()
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1628), Color(0xFF0F2438), Color(0xFF16243A), Color(0xFF0D1B2E))
    )
    val shimmerGradient = Brush.linearGradient(
        colors = listOf(Color.Transparent, Color(0x12C9962E), Color.Transparent),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 400f, 600f)
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(shimmerGradient))

        // Decorative blobs
        Box(modifier = Modifier.size(500.dp).offset(x = (-120).dp, y = (-180).dp)
            .background(Brush.radialGradient(colors = listOf(Color(0x14C9962E), Color.Transparent)), CircleShape))
        Box(modifier = Modifier.size(400.dp).offset(x = 140.dp, y = 220.dp)
            .background(Brush.radialGradient(colors = listOf(Color(0x0A1A7AFF), Color.Transparent)), CircleShape))

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            // Logo with pulse ring
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(150.dp)
                    .scale(pulseScale * logoScale.value)
                    .alpha(logoAlpha.value * 0.25f)
                    .background(Brush.radialGradient(colors = listOf(Color(0xFFC9962E), Color.Transparent)), CircleShape))

                Box(modifier = Modifier.size(110.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .background(Brush.radialGradient(colors = listOf(Color(0xFF1E3452), Color(0xFF0F2438))), CircleShape)
                    .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "OmSai Ventures Logo",
                        modifier = Modifier.size(76.dp).padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.alpha(textAlpha.value), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "OmSai Ventures", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, textAlign = TextAlign.Center, letterSpacing = 1.sp)
                Text(text = "ENTERPRISES", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFFC9962E), letterSpacing = 4.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(dividerWidth.value * 0.45f).height(1.dp)
                .background(Brush.horizontalGradient(colors = listOf(
                    Color.Transparent, Color(0xFFC9962E), Color(0xFFE8B84B), Color(0xFFC9962E), Color.Transparent))))

            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "AI-Powered Payroll Management", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = Color(0xFF8FA8C8), textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value), letterSpacing = 0.3.sp)

            Spacer(modifier = Modifier.height(56.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(taglineAlpha.value)) {
                listOf(dotAlpha1, dotAlpha2, dotAlpha3).forEach { anim ->
                    Box(modifier = Modifier.size(6.dp).alpha(0.3f + anim.value * 0.7f)
                        .background(Color(0xFFC9962E), CircleShape))
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).alpha(taglineAlpha.value)) {
            Text(text = "Secured  \u00B7  Intelligent  \u00B7  Enterprise", fontSize = 11.sp,
                color = Color(0xFF4A6080), letterSpacing = 1.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}
