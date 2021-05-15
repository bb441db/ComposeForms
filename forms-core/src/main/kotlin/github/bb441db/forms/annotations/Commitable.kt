package github.bb441db.forms.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Commitable(val generateExtension: Boolean = true, val fileName: String = "", val className: String = "")
