package ru.kmorozov.db.core.logic.model.book.google

/**
 * Created by km on 12.12.2015.
 */
enum class Resolutions(val resolution: Int) {

    _350px(350), _410px(410), _495px(495), _575px(575), _685px(685), _800px(800), _910px(910), _1042px(1042), _1280px(1280);

    override fun toString(): String {
        return String.format("%d px", resolution)
    }

    companion object {

        fun getEnum(value: Int): Resolutions {
            for (v in values())
                if (v.resolution == value) return v

            throw IllegalArgumentException()
        }
    }
}
