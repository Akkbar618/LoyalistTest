import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun LoyaltyCard() {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Логотип
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Лого", color = Color.White)
            }

            // Название
            Text(
                "Кофеек",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Прогресс-бар штампов
            StampProgressBar(
                totalStamps = 10,
                filledStamps = 5,
                stampColor = Color(0xFF2E7D32),
                columns = 5 // 5 штампов в ряду
            )
        }
    }
}

@Composable
fun StampProgressBar(
    totalStamps: Int,
    filledStamps: Int,
    stampColor: Color,
    columns: Int
) {
    // Вычисляем количество рядов
    val rows = (totalStamps + columns - 1) / columns

    // Создаем сетку штампов
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Вычисляем количество штампов в текущем ряду
                val startIndex = row * columns
                val endIndex = minOf(startIndex + columns, totalStamps)

                // Добавляем штампы в ряд
                for (index in startIndex until endIndex) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (index < filledStamps) stampColor else Color.White)
                            .border(1.dp, stampColor, CircleShape)
                    )
                }
            }
        }
    }
}