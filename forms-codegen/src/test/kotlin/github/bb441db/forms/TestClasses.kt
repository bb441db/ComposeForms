package github.bb441db.forms

import github.bb441db.forms.annotations.Commitable

@Commitable(generateExtension = false)
data class Test1(val foo: Boolean)

@Commitable(generateExtension = false)
data class Test2<T>(val foo: T)

@Commitable(generateExtension = false)
data class Test3(val foo: String, val bar: String)