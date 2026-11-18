package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CenterTestPrice
import com.example.data.model.DayChamberSchedule
import com.example.data.model.DoctorChamber
import com.example.data.model.FacilityType
import com.example.data.model.ScheduleSlot
import com.example.data.model.SlotStatus
import org.json.JSONArray
import org.json.JSONObject

class HealthcareConverters {

    @TypeConverter
    fun fromFacilityType(type: FacilityType): String = type.name

    @TypeConverter
    fun toFacilityType(raw: String): FacilityType =
        runCatching { FacilityType.valueOf(raw) }.getOrDefault(FacilityType.PRIVATE)

    @TypeConverter
    fun fromStringList(list: List<String>): String =
        JSONArray(list).toString()

    @TypeConverter
    fun toStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { idx -> arr.optString(idx) }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromChambers(chambers: List<DoctorChamber>): String {
        val arr = JSONArray()
        chambers.forEach { c ->
            val obj = JSONObject().apply {
                put("chamberId", c.chamberId)
                put("hospitalId", c.hospitalId)
                put("hospitalName", c.hospitalName)
                put("chamberName", c.chamberName)
                put("address", c.address)
                put("area", c.area)
                put("district", c.district)
                put("consultationFeeBdt", c.consultationFeeBdt)
                put("availableDaysSummary", c.availableDaysSummary)
                val schedArr = JSONArray()
                c.weeklySchedule.forEach { d ->
                    val dObj = JSONObject().apply {
                        put("weekdayIndex", d.weekdayIndex)
                        put("weekdayShort", d.weekdayShort)
                        put("isOpen", d.isOpen)
                        put("startTime24h", d.startTime24h)
                        put("endTime24h", d.endTime24h)
                        put("breakWindow", d.breakWindow)
                        put("slotDurationMins", d.slotDurationMins)
                        put("holidayNote", d.holidayNote ?: "")
                        val slotArr = JSONArray()
                        d.slots.forEach { s ->
                            slotArr.put(
                                JSONObject()
                                    .put("time24h", s.time24h)
                                    .put("status", s.status.name)
                            )
                        }
                        put("slots", slotArr)
                    }
                    schedArr.put(dObj)
                }
                put("weeklySchedule", schedArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    @TypeConverter
    fun toChambers(raw: String): List<DoctorChamber> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val schedArr = obj.optJSONArray("weeklySchedule") ?: JSONArray()
                val schedule = List(schedArr.length()) { j ->
                    val dObj = schedArr.getJSONObject(j)
                    val slotArr = dObj.optJSONArray("slots") ?: JSONArray()
                    val slots = List(slotArr.length()) { k ->
                        val sObj = slotArr.getJSONObject(k)
                        val status = runCatching {
                            SlotStatus.valueOf(sObj.optString("status", SlotStatus.AVAILABLE.name))
                        }.getOrDefault(SlotStatus.AVAILABLE)
                        ScheduleSlot(
                            time24h = sObj.optString("time24h"),
                            status = status
                        )
                    }
                    val note = dObj.optString("holidayNote").takeIf { it.isNotBlank() }
                    DayChamberSchedule(
                        weekdayIndex = dObj.optInt("weekdayIndex"),
                        weekdayShort = dObj.optString("weekdayShort"),
                        isOpen = dObj.optBoolean("isOpen"),
                        startTime24h = dObj.optString("startTime24h"),
                        endTime24h = dObj.optString("endTime24h"),
                        breakWindow = dObj.optString("breakWindow"),
                        slotDurationMins = dObj.optInt("slotDurationMins", 15),
                        slots = slots,
                        holidayNote = note
                    )
                }
                DoctorChamber(
                    chamberId = obj.optString("chamberId"),
                    hospitalId = obj.optString("hospitalId"),
                    hospitalName = obj.optString("hospitalName"),
                    chamberName = obj.optString("chamberName"),
                    address = obj.optString("address"),
                    area = obj.optString("area"),
                    district = obj.optString("district"),
                    consultationFeeBdt = obj.optInt("consultationFeeBdt"),
                    availableDaysSummary = obj.optString("availableDaysSummary"),
                    weeklySchedule = schedule
                )
            }
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromCenterPrices(prices: List<CenterTestPrice>): String {
        val arr = JSONArray()
        prices.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("centerId", p.centerId)
                    put("centerName", p.centerName)
                    put("area", p.area)
                    put("district", p.district)
                    put("priceBdt", p.priceBdt)
                    put("homeCollectionFeeBdt", p.homeCollectionFeeBdt)
                    put("homeCollectionAvailable", p.homeCollectionAvailable)
                    put("reportDeliveryHours", p.reportDeliveryHours)
                }
            )
        }
        return arr.toString()
    }

    @TypeConverter
    fun toCenterPrices(raw: String): List<CenterTestPrice> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                CenterTestPrice(
                    centerId = obj.optString("centerId"),
                    centerName = obj.optString("centerName"),
                    area = obj.optString("area"),
                    district = obj.optString("district"),
                    priceBdt = obj.optInt("priceBdt"),
                    homeCollectionFeeBdt = obj.optInt("homeCollectionFeeBdt"),
                    homeCollectionAvailable = obj.optBoolean("homeCollectionAvailable"),
                    reportDeliveryHours = obj.optInt("reportDeliveryHours")
                )
            }
        }.getOrDefault(emptyList())
    }
}
