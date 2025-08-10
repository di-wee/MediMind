package com.example.medimind.adapters

sealed class HistoryRow {
    data class DateHeader(val date: String) : HistoryRow()
    data class StatusHeader(val label: String, val count: Int, val isMissed: Boolean) : HistoryRow()
    data class MedGroupHeader(val name: String) : HistoryRow()
    data class TimeRow(val time: String, val taken: Boolean) : HistoryRow()
}
