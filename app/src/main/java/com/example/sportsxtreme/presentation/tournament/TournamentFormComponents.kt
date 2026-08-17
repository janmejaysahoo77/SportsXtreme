package com.example.sportsxtreme.presentation.tournament

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        RequiredTitle(title)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(17.dp), clip = false)
                .clip(RoundedCornerShape(17.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF101827), Color(0xFF0B111C))))
                .border(1.dp, Color(0xFF31405C), RoundedCornerShape(17.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            content = content
        )
    }
}

@Composable
internal fun LabeledInput(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, trailingIcon: @Composable (() -> Unit)? = null) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        RequiredTitle(label, compact = true)
        BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold), singleLine = true, cursorBrush = SolidColor(Color.White)) { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(13.dp)).background(Brush.verticalGradient(listOf(Color(0xFF172033), FormField))).border(1.dp, Color(0xFF42526F), RoundedCornerShape(13.dp)).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) Text(placeholder, color = Color(0xFF768784), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        innerTextField()
                    }
                    trailingIcon?.invoke()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateBox(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onValueChange(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))) }
                showDatePicker = false
            }) { Text("OK") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }) { DatePicker(state = datePickerState) }
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = Color(0xFF99A9A5), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold), singleLine = true, readOnly = true, cursorBrush = SolidColor(Color.White)) { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(13.dp)).background(FormField).border(1.dp, Color(0xFF42526F), RoundedCornerShape(13.dp)).clickable { showDatePicker = true }.padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.outline_calendar_add_on_24), "Select date", tint = FormAccent, modifier = Modifier.size(19.dp))
                    Box(modifier = Modifier.padding(start = 10.dp)) { if (value.isEmpty()) Text("DD/MM/YYYY", color = Color(0xFFE5ECE9), fontSize = 14.sp, fontWeight = FontWeight.Bold) else innerTextField() }
                }
            }
        }
    }
}

@Composable
internal fun ChipRow(labels: List<String>, selectedLabel: String, onLabelSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        labels.forEach { label ->
            val selected = label == selectedLabel
            Box(modifier = Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (selected) FormAccent else Color(0xFF1A2231)).border(1.dp, if (selected) Color(0xFFDFFF6C) else Color(0xFF34405A), RoundedCornerShape(18.dp)).clickable { onLabelSelected(label) }.padding(horizontal = 15.dp), contentAlignment = Alignment.Center) {
                Text(label, color = if (selected) Color(0xFF111604) else Color(0xFFBBC7C4), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
internal fun BallChoice(label: String, imageRes: Int, selected: Boolean, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFF223018), Color(0xFF080D12)))).border(if (selected) 2.dp else 1.dp, if (selected) FormAccent else Color(0xFF463629), CircleShape).padding(7.dp), contentAlignment = Alignment.Center) {
            Image(painterResource(imageRes), "$label ball", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
        Text(label, color = if (selected) FormAccent else Color(0xFFB9C3C0), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
internal fun TogglePanel(title: String, subtitle: String, active: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().shadow(7.dp, RoundedCornerShape(16.dp), clip = false).clip(RoundedCornerShape(16.dp)).background(if (active) Color(0xFF101A14) else FormPanel).border(1.dp, if (active) Color(0x994F6B1F) else FormStroke, RoundedCornerShape(16.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(if (active) FormAccent else Color.Transparent).border(1.dp, if (active) FormAccent else Color(0xFF3B455B), CircleShape), contentAlignment = Alignment.Center) {
            if (active) Icon(painterResource(R.drawable.baseline_check_circle_24), null, tint = Color(0xFF111604), modifier = Modifier.size(15.dp))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = FormMuted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
internal fun NextButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(58.dp).shadow(12.dp, RoundedCornerShape(18.dp), clip = false).clip(RoundedCornerShape(18.dp)).background(Brush.horizontalGradient(listOf(FormAccent, Color(0xFFDFFF6C), FormWarm))).clickable(enabled = !isLoading, onClick = onClick), contentAlignment = Alignment.Center) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF111604), strokeWidth = 2.dp) else Text("Next", color = Color(0xFF111604), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
internal fun RequiredTitle(label: String, compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = if (compact) 12.sp else 16.sp, fontWeight = FontWeight.ExtraBold)
        Text("*", color = FormAccent, fontSize = if (compact) 12.sp else 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
internal fun SectionSmallLabel(text: String) = Text(text, color = Color(0xFF99A9A5), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)

@Composable
internal fun AddBadge(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(58.dp).clip(CircleShape).background(Brush.radialGradient(listOf(FormAccent.copy(alpha = 0.22f), Color(0xFF071109)))).border(2.dp, FormAccent.copy(alpha = 0.85f), CircleShape), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("+", color = FormAccent, fontSize = 23.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
            Text("LOGO", color = FormAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
internal fun CheckPill(modifier: Modifier = Modifier, accent: Color) {
    Box(modifier = modifier.size(27.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.baseline_check_circle_24), null, tint = Color(0xFF111604), modifier = Modifier.size(15.dp))
    }
}
