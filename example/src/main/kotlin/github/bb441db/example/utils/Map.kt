package github.bb441db.example.utils

fun Map<String, Array<String>>.firstOrNull(name: String): String? {
    return this[name]?.firstOrNull()
}

fun Map<String, Array<String>>.first(name: String): String {
    return this.getValue(name).first()
}