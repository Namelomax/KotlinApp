import Client.Companion.passw
import Client.Companion.password
import Client.Companion.url
import Client.Companion.usernam
import Client.Companion.users
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.*

data class User(val username: String, val hashedPassword: String)
fun registerUser(username: String, password: String): Boolean {
    if (users.any { it.username == username }) {
        println("Username is already used")
        return false
    }
    if (!username.any { it.isLetter() }) {
        println("Username must contain letters")
        return false
    }
    if (username.length < 3) {
        println("Username must be more than 3 characters")
        return false
    }
    if (password.length < 3) {
        println("Password must be more than 3 characters")
        return false
    }

    val salt = generateSalt()
    val hashedPassword = sha512(passw)

    try {
        DriverManager.getConnection(url, "postgres", "90522468q").use { connection ->
            val sql = "INSERT INTO users (name, password, salt) VALUES (?, ?, ?)"
            val statement = connection.prepareStatement(sql)
            statement.setString(1, username)
            statement.setBytes(2, hashedPassword.toByteArray())
            statement.setBytes(3, salt)
            statement.executeUpdate()
        }
        println("Registration successful")
        return true
    } catch (e: SQLException) {
        // Handle database errors
        e.printStackTrace()
        return false
    }
}
fun authenticateUser(username: String, providedPassword: String): Boolean {
    try {
        val connection: Connection = DriverManager.getConnection(url, "postgres", "90522468q")
        val sql = "SELECT password, salt FROM users WHERE name = ?"
        val preparedStatement: PreparedStatement = connection.prepareStatement(sql)
        preparedStatement.setString(1, username)
        val resultSet: ResultSet = preparedStatement.executeQuery()
                if (resultSet.next()) { val storedPasswordHash = resultSet.getString("password")

            val salt = resultSet.getString("salt")

            // Hash the user-entered password using the retrieved salt
            val providedPasswordHash = hashPassword(providedPassword, salt)

            // Compare the newly hashed password to the stored hashed password
            if (providedPasswordHash == storedPasswordHash) {
                preparedStatement.close()
                connection.close()
                return true
            }else if (providedPassword== passw){preparedStatement.close()
                connection.close()
                return true}
        }
        preparedStatement.close()
        connection.close()
        println("invalid pass or login")
        return false
    } catch (e: SQLException) {
        // Handle database errors
        e.printStackTrace()
        return false
    }
}

fun hashPassword(password: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-512")
    val saltedPassword = password + salt
    md.update(saltedPassword.toByteArray())
    val hashedPassword = md.digest()
    return hashedPassword.joinToString("") { "%02x".format(it) }
}
fun sha512(input: String): String {
    try {
        // Create a MessageDigest instance for SHA-512
        val md = MessageDigest.getInstance("SHA-512")

        // Update the message digest with the input bytes
        val inputBytes = input.toByteArray()
        val hashBytes = md.digest(inputBytes)

        // Convert the hash bytes to a hexadecimal string representation
        val hexString = StringBuilder(2 * hashBytes.size)
        for (byte in hashBytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }

        return hexString.toString()
    } catch (e: Exception) {
        // Handle any exceptions that may occur during hashing (e.g., NoSuchAlgorithmException)
        e.printStackTrace()
        throw RuntimeException("SHA-512 hashing failed")
    }
}
fun generateSalt(): ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
}
