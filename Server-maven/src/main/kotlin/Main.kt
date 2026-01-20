import BaseClasses.Coordinates
import BaseClasses.HumanBeing
import Commands.CommandList
import Commands.Invoker
import com.google.gson.*
import com.google.gson.internal.LinkedTreeMap
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.time.LocalDateTime
import java.util.*
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

var scriptlist = emptyArray<String>()
var dateOfInitialization: LocalDateTime = LocalDateTime.of(
    LocalDateTime.now().year,
    LocalDateTime.now().monthValue, LocalDateTime.now().dayOfMonth,
    LocalDateTime.now().hour, LocalDateTime.now().minute, LocalDateTime.now().second
)
var listOfData = LinkedList<LinkedTreeMap<String, Any?>>()
val connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydatabase", "postgres", "90522468q")
var listOfHumanBeing = LinkedList<HumanBeing>()
var ongoing = true
var line: String = ""
val cl = CommandList()
val invoker = Invoker()
val commandsArray = JsonArray()
private const val PORT = 12345
var response =""
var refactor = LinkedTreeMap<String, Any?>()
const val url = "jdbc:postgresql://localhost:5432/mydatabase"
const val username = "user2"
const val password = "12345"
var username1 = ""
var password1 = ""
fun main() {
    val readWriteLock: ReadWriteLock = ReentrantReadWriteLock()
    val socket = DatagramSocket(PORT)
    var start =true
    cl.setCommands()
     listOfData = getDataFromDatabase()
    makeListOfHumanBeing()
    while (true) {
        // receive a UDP packet
        if (start) {
            println("Server is ready")
            start = false
        }
        val buffer = ByteArray(8192)
        val responsePacket1 = DatagramPacket(buffer, buffer.size)
        socket.receive(responsePacket1)
        val responseString = String(responsePacket1.data, 0, responsePacket1.length)
        val achievedJson: JsonElement = Gson().fromJson(responseString, JsonElement::class.java)
        readWriteLock.writeLock().lock()
        if (achievedJson.isJsonArray) {
            // Situation 1: JSON is an array of commands
            val jsonArray = achievedJson as JsonArray
            for (element in jsonArray) {
                val responseCommand = element.asJsonObject.get("command").asString
                println("Received message from client: $responseCommand")
                cl.commands[responseCommand]?.let { invoker.setCommand(it) }
                invoker.executeCommand()
                val newJsonArray = element.asJsonArray.asJsonArray
                commandsArray.addAll(newJsonArray)
                val commandJson = JsonObject()
                commandJson.addProperty("answer", response)
                commandsArray.add(commandJson)
                val jsonString = commandsArray.toString()
                println(jsonString)
            }
            val jsonString = commandsArray.toString()
            val address = responsePacket1.address
            val responsePacket =
                DatagramPacket(jsonString.toByteArray(), jsonString.toByteArray().size, address, responsePacket1.port)
            socket.send(responsePacket)
        }
        if (achievedJson.isJsonObject) {
            val jsonObject = achievedJson as JsonObject
            username1 = jsonObject.get("username")?.asString ?: ""
            password1 = jsonObject.get("password")?.asString ?: ""
            val responseCommand = jsonObject.get("command").asString

            val paramValue = jsonObject.get("param")
            println(paramValue)
            // Situation 2: JSON is a command with "param" as a string
            if (paramValue != null && paramValue.isJsonPrimitive) {
                val paramWithoutQuotes = paramValue.asString.replace("\"", "")
                line = paramWithoutQuotes
                println(paramWithoutQuotes)
                println(line)
                println("Received message from client: $responseCommand")
                cl.commands[responseCommand]?.let { invoker.setCommand(it) }
                invoker.executeCommand()
            }

            // Situation 3: JSON is a command with "param" as an object
            else if (paramValue != null && paramValue.isJsonObject) {
                val refactorObject = paramValue as JsonObject
                for ((key, value) in refactorObject.entrySet()) {
                    refactor[key] = value
                }
                println(refactor)
                println("Received message from client: $responseCommand")
                cl.commands[responseCommand]?.let { invoker.setCommand(it) }
                invoker.executeCommand()
            }

            // Situation 4: JSON is a command with "param" as a nested map
            else if (paramValue != null && paramValue.isJsonNull) {
                val paramWithoutQuotes = paramValue.asString.replace("\"", "")
                val refactorObject = Gson().fromJson(paramWithoutQuotes, JsonObject::class.java)
                for ((key, value) in refactorObject.entrySet()) {
                    refactor[key] = value
                }
            }

            // Send the response
            val responseJson = JsonObject()
            responseJson.addProperty("answer", response)
            val jsonString = responseJson.toString()
            val address = responsePacket1.address
            val responsePacket =
                DatagramPacket(jsonString.toByteArray(), jsonString.toByteArray().size, address, responsePacket1.port)
            socket.send(responsePacket)
            println(jsonString)
        }
    }


}
fun makeListOfHumanBeing() {
    dateOfInitialization = LocalDateTime.of(
        LocalDateTime.now().year,
        LocalDateTime.now().monthValue, LocalDateTime.now().dayOfMonth,
        LocalDateTime.now().hour, LocalDateTime.now().minute, LocalDateTime.now().second
    )
    //val maxId = listOfData.maxOf { (it["id"] as Double?)?.toInt() ?: 1 }
    //writeInTxtFile(pathToId, (maxId).toString())
    val groupedById = listOfData.groupBy { it["id"] }
    for (datum in groupedById) {
        val unit = HumanBeing(datum.value[0])
        listOfHumanBeing.add(unit)
    }
}
fun getDataFromDatabase(): LinkedList<LinkedTreeMap<String, Any?>> {

    val listOfData = LinkedList<LinkedTreeMap<String, Any?>>()

    try {
        DriverManager.getConnection(url, username, password).use { conn ->
            val query = "SELECT * FROM People"

            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(query)

                while (rs.next()) {
                    val map = LinkedTreeMap<String, Any?>()

                    map["id"] = rs.getInt("id")
                    map["name"] = rs.getString("name")
                    map["coordinates"] = rs.getArray("coordinates")
                    map["creationDate"] = rs.getString("creationDate")
                    map["realHero"] = rs.getBoolean("realHero")
                    map["hasToothpick"] = rs.getBoolean("hasToothpick")
                    map["impactSpeed"] = rs.getLong("impactSpeed")
                    map["soundtrackName"] = rs.getString("soundtrackName")
                    map["minutesOfWaiting"] = rs.getDouble("minutesOfWaiting")
                    map["mood"] = rs.getString("mood")
                    map["car"] = rs.getString("car")

                    listOfData.add(map)
                }
            }
        }
    } catch (e: SQLException) {
        e.printStackTrace()
    }

    return listOfData
}
fun insertUnit(unit: LinkedTreeMap<String, Any?>) {

    val statement = connection.prepareStatement("INSERT INTO people (name, coordinates, creationdate, realhero, hastoothpick, impactspeed, soundtrackname, minutesofwaiting, mood, car) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
    statement.setString(1, unit["name"].toString())
    val coordinates = (unit["coordinates"] as? Coordinates)
    val coordinatesList = coordinates?.getCoordinates() ?: emptyList()
    val javaArray = coordinatesList.map { it.toDouble() }.toTypedArray()
    val array = connection.createArrayOf("NUMERIC", javaArray)
    statement.setArray(2, array)
    statement.setString(3, unit["creationDate"].toString())
    statement.setBoolean(4, unit["realHero"] as Boolean)
    statement.setBoolean(5, unit["hasToothpick"] as Boolean)
    statement.setLong(6, unit["impactSpeed"].toString().toLong())
    statement.setString(7, unit["soundtrackName"].toString())
    statement.setDouble(8, unit["minutesOfWaiting"].toString().toDouble())
    statement.setString(9, unit["mood"].toString().uppercase())
    statement.setString(10, unit["car"].toString())

    statement.executeUpdate()

    statement.close()
    connection.close()
}
