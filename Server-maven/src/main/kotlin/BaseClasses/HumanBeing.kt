package BaseClasses

import Second_sem.lab5.Kotlin.BaseClasses.*
import com.google.gson.JsonPrimitive
import com.google.gson.internal.LinkedTreeMap
import password
import password1
import username
import username1
import java.time.LocalDateTime

class HumanBeing(
    val id: Int?, val name: String="DefaultName", val coordinates: Coordinates = Coordinates(),
    val creationDate: LocalDateTime? = LocalDateTime.of(LocalDateTime.now().year,
        LocalDateTime.now().monthValue, LocalDateTime.now().dayOfMonth,
        LocalDateTime.now().hour, LocalDateTime.now().minute, LocalDateTime.now().second),
    val realHero: Boolean = false, val hasToothpick: Boolean = false, val impactSpeed: Long = 0,
    val soundtrackName: String = "DefaultSoundtrackName", val minutesOfWaiting: Double? = null,
    val mood: Mood? = null, val car: Car? = null, val owner: String="Defaultowner",val password: String="Defaultpassw",) : Comparable<HumanBeing>  {


    constructor(linkedTreeMap: LinkedTreeMap<String, Any?>) : this(
        (linkedTreeMap["id"] as? Int)?.toInt(),
        (linkedTreeMap["name"] as? JsonPrimitive).toString() ?: "DefaultName",
        Coordinates(linkedTreeMap["coordinates"] as? List<Number> ?: listOf(0.0, 0.0)),
        makeLocalDateTime(
            linkedTreeMap["creationDate"] as? List<Int> ?:
            listOf(
                LocalDateTime.now().year, LocalDateTime.now().monthValue,
                LocalDateTime.now().dayOfMonth, LocalDateTime.now().hour,
                LocalDateTime.now().minute, LocalDateTime.now().second
            )
        ),
        linkedTreeMap["realHero"] as? Boolean ?: false,
        linkedTreeMap["hasToothpick"] as? Boolean ?: false,
        (linkedTreeMap["impactSpeed"] as? Double ?: 0.0).toLong(),
        linkedTreeMap["soundtrackName"] as? String ?: "DefaultSoundtrackName",
        linkedTreeMap["minutesOfWaiting"] as? Double,
        (linkedTreeMap["mood"] as? String)?.let { Mood.valueOf(it) },
        Car(linkedTreeMap["car"] as? String),
        linkedTreeMap["owner"] as? String ?: "DefaultName"
    )

    override fun compareTo(other: HumanBeing): Int {
        return other.id?.let { id?.compareTo(it) ?: 0 }!!
    }
    override fun toString(): String {
        return """
            
            id: $id
            name: $name
            coordinates: $coordinates
            creation date: $creationDate
            real hero: $realHero
            has toothpick: $hasToothpick
            impact speed: $impactSpeed
            soundtrack name: $soundtrackName
            minutes of waiting: $minutesOfWaiting
            mood: $mood
            car: $car
            owner: $mood
            password: $car
            """.trimIndent()
    }


    fun makeLinkedTreeMap():LinkedTreeMap<String, Any?>{
        val list = LinkedTreeMap<String, Any?>()
        list["name"] = name
        list["coordinates"] = coordinates
        list["creationDate"] = creationDate
        list["realHero"] = realHero
        list["hasToothpick"] = hasToothpick
        list["impactSpeed"] = impactSpeed
        list["soundtrackName"] = soundtrackName
        list["minutesOfWaiting"] = minutesOfWaiting
        list["mood"] = mood
        list["car"] = car
        list["owner"]=username1
        return list
    }
}