package com.diraj.kreddit.utils

import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.*

fun format(value: Long, suffixes: NavigableMap<Long, String>): String {
    if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1, suffixes)
    if (value < 0) return "-" + format(-value, suffixes)
    if (value < 1000) return value.toString()
    val entry: Map.Entry<Long, String> = suffixes.floorEntry(value)!!
    val divideBy = entry.key
    val suffix = entry.value
    val truncated = value / (divideBy / 10)
    val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
    return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
}

fun Int.getPrettyCount(): String {
    val suffixes: NavigableMap<Long, String> = TreeMap()
    suffixes[1_000L] = "k"
    suffixes[1_000_000L] = "M"
    suffixes[1_000_000_000L] = "G"
    suffixes[1_000_000_000_000L] = "T"
    suffixes[1_000_000_000_000_000L] = "P"
    suffixes[1_000_000_000_000_000_000L] = "E"

    return format(this.toLong(), suffixes)
}

fun <T> androidLazy(initializer: () -> T) : Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <reified T : ViewModel> Fragment.getViewModel(viewModelFactory: ViewModelProvider.Factory): T =
    ViewModelProvider(this, viewModelFactory)[T::class.java]

fun <T : Parcelable>T.deepCopy(): T {
    var parcel: Parcel? = null
    return try {
        parcel = Parcel.obtain()
        parcel.writeParcelable(this, 0)
        parcel.setDataPosition(0)
        parcel.readParcelable(this::class.java.classLoader)!!
    } finally {
        parcel?.recycle()
    }
}
