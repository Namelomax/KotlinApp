import java.security.MessageDigest

data class User(val username: String, val hashedPassword: String)
val users = mutableListOf<User>()
fun registerUser(username: String, password: String) {

    val hashedPassword = hashPassword(password)
    val user = User(username, hashedPassword)
    users.add(user)
}
fun authenticateUser(username: String, password: String): Boolean {
    val user = users.find { it.username == username }
    if (user != null) {
        val hashedPassword = hashPassword(password)
        return user.hashedPassword == hashedPassword
    }
    return false
}
fun hashPassword(password: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val bytes = messageDigest.digest(password.toByteArray(Charsets.UTF_8))
    val hexString = StringBuilder(2 * bytes.size)

    for (byte in bytes) {
        val hex = Integer.toHexString(0xFF and byte.toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }

    return hexString.toString()
}