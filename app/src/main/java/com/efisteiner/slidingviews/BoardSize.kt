package com.efisteiner.slidingviews

import android.os.Parcel
import android.os.Parcelable

enum class BoardSize(val numCards: Int): Parcelable {
    SMALL(9),
    MEDIUM(16),
    LARGE(25);

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BoardSize> {
        override fun createFromParcel(source: Parcel): BoardSize {
            // Read the ordinal to identify the enum constant
            return entries[source.readInt()]
        }

        override fun newArray(size: Int): Array<BoardSize?> {
            return arrayOfNulls(size)
        }

    }
    fun getWidth(): Int {
        return when (this) {
            SMALL -> 9
            MEDIUM -> 16
            LARGE -> 25
        }
    }

    fun getHeight(): Int {
        return numCards / getWidth()
    }

    fun getNumPairs(): Int {
        return numCards / 2
    }
}