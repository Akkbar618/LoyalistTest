package com.example.loyalisttest.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.loyalisttest.R

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource

//
//@Preview
//@Composable
//fun Comps(){
//    val vector = ImageVector.vectorResource(id = R.drawable.search)
//
//    Box(
//        modifier = Modifier
//            .background(Color.White)
////            .fillMaxSize()
//    ){
//
//        Image(
//            painter = painterResource(id = R.drawable.img),
//            contentDescription = "Пример изображения",
//            modifier = Modifier
//                .size(200.dp)
//        )
//
//        Spacer(modifier = Modifier.height(20.dp).width(20.dp))
//
//        Text("Cofeeee")
//
//        Spacer(modifier = Modifier.height(20.dp).width(20.dp))
//
//
//
//    }
//}

@Preview
@Composable
fun Comp(){
    val vector = ImageVector.vectorResource(id = R.drawable.search)

    Image(
        imageVector = vector,
        contentDescription = "Набранные очки",
        modifier = Modifier
            .clip(CircleShape)
            .size(150.dp)
            .background(Color.Green)
            .padding(8.dp)
        ,
        alignment = Alignment.Center,
        )
}