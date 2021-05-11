package github.bb441db.example.data

import github.bb441db.forms.annotations.Commitable

@Commitable
data class ExampleWithTypeParameters<T, X>(val foo: T, val bar: X?)
