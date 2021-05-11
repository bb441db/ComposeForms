package github.bb441db.example.data

import github.bb441db.forms.annotations.Commitable

@Commitable
data class Example(val foo: Boolean, val bar: String?, val fooBar: String?)

