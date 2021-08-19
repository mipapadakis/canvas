package com.mipapadakis.canvas.ui

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.mipapadakis.canvas.R

class CanvasColor(val colorID: Int){
    val colorName: String = getColorNameFromId(colorID)

    companion object {
        private val colorNameAndIdList = initColorNameAndIdList()
        private val basicColorNameAndIdList = initBasicColorNameAndIdList()

        private fun initColorNameAndIdList(): ArrayList<ColorNameAndId> {
            val colorList = ArrayList<ColorNameAndId>()
            colorList.add(ColorNameAndId("AliceBlue", R.color.AliceBlue))
            colorList.add(ColorNameAndId("AntiqueWhite", R.color.AntiqueWhite))
            colorList.add(ColorNameAndId("Aqua", R.color.Aqua))
            colorList.add(ColorNameAndId("Aquamarine", R.color.Aquamarine))
            colorList.add(ColorNameAndId("Azure", R.color.Azure))
            colorList.add(ColorNameAndId("Beige", R.color.Beige))
            colorList.add(ColorNameAndId("Bisque", R.color.Bisque))
            colorList.add(ColorNameAndId("Black", R.color.Black))
            colorList.add(ColorNameAndId("BlanchedAlmond", R.color.BlanchedAlmond))
            colorList.add(ColorNameAndId("Blue", R.color.Blue))
            colorList.add(ColorNameAndId("BlueViolet", R.color.BlueViolet))
            colorList.add(ColorNameAndId("Brown", R.color.Brown))
            colorList.add(ColorNameAndId("BurlyWood", R.color.BurlyWood))
            colorList.add(ColorNameAndId("CadetBlue", R.color.CadetBlue))
            colorList.add(ColorNameAndId("Chartreuse", R.color.Chartreuse))
            colorList.add(ColorNameAndId("Chocolate", R.color.Chocolate))
            colorList.add(ColorNameAndId("Coral", R.color.Coral))
            colorList.add(ColorNameAndId("CornflowerBlue", R.color.CornflowerBlue))
            colorList.add(ColorNameAndId("Cornsilk", R.color.Cornsilk))
            colorList.add(ColorNameAndId("Crimson", R.color.Crimson))
            colorList.add(ColorNameAndId("Cyan", R.color.Cyan))
            colorList.add(ColorNameAndId("DarkBlue", R.color.DarkBlue))
            colorList.add(ColorNameAndId("DarkCyan", R.color.DarkCyan))
            colorList.add(ColorNameAndId("DarkGoldenRod", R.color.DarkGoldenRod))
            colorList.add(ColorNameAndId("DarkGray", R.color.DarkGray))
            colorList.add(ColorNameAndId("DarkGreen", R.color.DarkGreen))
            colorList.add(ColorNameAndId("DarkKhaki", R.color.DarkKhaki))
            colorList.add(ColorNameAndId("DarkMagenta", R.color.DarkMagenta))
            colorList.add(ColorNameAndId("DarkOliveGreen", R.color.DarkOliveGreen))
            colorList.add(ColorNameAndId("DarkOrange", R.color.DarkOrange))
            colorList.add(ColorNameAndId("DarkOrchid", R.color.DarkOrchid))
            colorList.add(ColorNameAndId("DarkRed", R.color.DarkRed))
            colorList.add(ColorNameAndId("DarkSalmon", R.color.DarkSalmon))
            colorList.add(ColorNameAndId("DarkSeaGreen", R.color.DarkSeaGreen))
            colorList.add(ColorNameAndId("DarkSlateBlue", R.color.DarkSlateBlue))
            colorList.add(ColorNameAndId("DarkSlateGray", R.color.DarkSlateGray))
            colorList.add(ColorNameAndId("DarkTurquoise", R.color.DarkTurquoise))
            colorList.add(ColorNameAndId("DarkViolet", R.color.DarkViolet))
            colorList.add(ColorNameAndId("DeepPink", R.color.DeepPink))
            colorList.add(ColorNameAndId("DeepSkyBlue", R.color.DeepSkyBlue))
            colorList.add(ColorNameAndId("DimGray", R.color.DimGray))
            colorList.add(ColorNameAndId("DodgerBlue", R.color.DodgerBlue))
            colorList.add(ColorNameAndId("FireBrick", R.color.FireBrick))
            colorList.add(ColorNameAndId("FloralWhite", R.color.FloralWhite))
            colorList.add(ColorNameAndId("ForestGreen", R.color.ForestGreen))
            colorList.add(ColorNameAndId("Fuchsia", R.color.Fuchsia))
            colorList.add(ColorNameAndId("Gainsboro", R.color.Gainsboro))
            colorList.add(ColorNameAndId("GhostWhite", R.color.GhostWhite))
            colorList.add(ColorNameAndId("Gold", R.color.Gold))
            colorList.add(ColorNameAndId("GoldenRod", R.color.GoldenRod))
            colorList.add(ColorNameAndId("Gray", R.color.Gray))
            colorList.add(ColorNameAndId("Green", R.color.Green))
            colorList.add(ColorNameAndId("GreenYellow", R.color.GreenYellow))
            colorList.add(ColorNameAndId("HoneyDew", R.color.HoneyDew))
            colorList.add(ColorNameAndId("HotPink", R.color.HotPink))
            colorList.add(ColorNameAndId("IndianRed", R.color.IndianRed))
            colorList.add(ColorNameAndId("Indigo", R.color.Indigo))
            colorList.add(ColorNameAndId("Ivory", R.color.Ivory))
            colorList.add(ColorNameAndId("Khaki", R.color.Khaki))
            colorList.add(ColorNameAndId("Lavender", R.color.Lavender))
            colorList.add(ColorNameAndId("LavenderBlush", R.color.LavenderBlush))
            colorList.add(ColorNameAndId("LawnGreen", R.color.LawnGreen))
            colorList.add(ColorNameAndId("LemonChiffon", R.color.LemonChiffon))
            colorList.add(ColorNameAndId("LightBlue", R.color.LightBlue))
            colorList.add(ColorNameAndId("LightCoral", R.color.LightCoral))
            colorList.add(ColorNameAndId("LightCyan", R.color.LightCyan))
            colorList.add(ColorNameAndId("LightGoldenRodYellow", R.color.LightGoldenRodYellow))
            colorList.add(ColorNameAndId("LightGray", R.color.LightGray))
            colorList.add(ColorNameAndId("LightGreen", R.color.LightGreen))
            colorList.add(ColorNameAndId("LightPink", R.color.LightPink))
            colorList.add(ColorNameAndId("LightSalmon", R.color.LightSalmon))
            colorList.add(ColorNameAndId("LightSeaGreen", R.color.LightSeaGreen))
            colorList.add(ColorNameAndId("LightSkyBlue", R.color.LightSkyBlue))
            colorList.add(ColorNameAndId("LightSlateGray", R.color.LightSlateGray))
            colorList.add(ColorNameAndId("LightSteelBlue", R.color.LightSteelBlue))
            colorList.add(ColorNameAndId("LightYellow", R.color.LightYellow))
            colorList.add(ColorNameAndId("Lime", R.color.Lime))
            colorList.add(ColorNameAndId("LimeGreen", R.color.LimeGreen))
            colorList.add(ColorNameAndId("Linen", R.color.Linen))
            colorList.add(ColorNameAndId("Magenta", R.color.Magenta))
            colorList.add(ColorNameAndId("Maroon", R.color.Maroon))
            colorList.add(ColorNameAndId("MediumAquaMarine", R.color.MediumAquaMarine))
            colorList.add(ColorNameAndId("MediumBlue", R.color.MediumBlue))
            colorList.add(ColorNameAndId("MediumOrchid", R.color.MediumOrchid))
            colorList.add(ColorNameAndId("MediumPurple", R.color.MediumPurple))
            colorList.add(ColorNameAndId("MediumSeaGreen", R.color.MediumSeaGreen))
            colorList.add(ColorNameAndId("MediumSlateBlue", R.color.MediumSlateBlue))
            colorList.add(ColorNameAndId("MediumSpringGreen", R.color.MediumSpringGreen))
            colorList.add(ColorNameAndId("MediumTurquoise", R.color.MediumTurquoise))
            colorList.add(ColorNameAndId("MediumVioletRed", R.color.MediumVioletRed))
            colorList.add(ColorNameAndId("MidnightBlue", R.color.MidnightBlue))
            colorList.add(ColorNameAndId("MintCream", R.color.MintCream))
            colorList.add(ColorNameAndId("MistyRose", R.color.MistyRose))
            colorList.add(ColorNameAndId("Moccasin", R.color.Moccasin))
            colorList.add(ColorNameAndId("NavajoWhite", R.color.NavajoWhite))
            colorList.add(ColorNameAndId("Navy", R.color.Navy))
            colorList.add(ColorNameAndId("OldLace", R.color.OldLace))
            colorList.add(ColorNameAndId("Olive", R.color.Olive))
            colorList.add(ColorNameAndId("OliveDrab", R.color.OliveDrab))
            colorList.add(ColorNameAndId("Orange", R.color.Orange))
            colorList.add(ColorNameAndId("OrangeRed", R.color.OrangeRed))
            colorList.add(ColorNameAndId("Orchid", R.color.Orchid))
            colorList.add(ColorNameAndId("PaleGoldenRod", R.color.PaleGoldenRod))
            colorList.add(ColorNameAndId("PaleGreen", R.color.PaleGreen))
            colorList.add(ColorNameAndId("PaleTurquoise", R.color.PaleTurquoise))
            colorList.add(ColorNameAndId("PaleVioletRed", R.color.PaleVioletRed))
            colorList.add(ColorNameAndId("PapayaWhip", R.color.PapayaWhip))
            colorList.add(ColorNameAndId("PeachPuff", R.color.PeachPuff))
            colorList.add(ColorNameAndId("Peru", R.color.Peru))
            colorList.add(ColorNameAndId("Pink", R.color.Pink))
            colorList.add(ColorNameAndId("Plum", R.color.Plum))
            colorList.add(ColorNameAndId("PowderBlue", R.color.PowderBlue))
            colorList.add(ColorNameAndId("Purple", R.color.Purple))
            colorList.add(ColorNameAndId("Red", R.color.Red))
            colorList.add(ColorNameAndId("RosyBrown", R.color.RosyBrown))
            colorList.add(ColorNameAndId("RoyalBlue", R.color.RoyalBlue))
            colorList.add(ColorNameAndId("SaddleBrown", R.color.SaddleBrown))
            colorList.add(ColorNameAndId("Salmon", R.color.Salmon))
            colorList.add(ColorNameAndId("SandyBrown", R.color.SandyBrown))
            colorList.add(ColorNameAndId("SeaGreen", R.color.SeaGreen))
            colorList.add(ColorNameAndId("SeaShell", R.color.SeaShell))
            colorList.add(ColorNameAndId("Sienna", R.color.Sienna))
            colorList.add(ColorNameAndId("Silver", R.color.Silver))
            colorList.add(ColorNameAndId("SkyBlue", R.color.SkyBlue))
            colorList.add(ColorNameAndId("SlateBlue", R.color.SlateBlue))
            colorList.add(ColorNameAndId("SlateGray", R.color.SlateGray))
            colorList.add(ColorNameAndId("Snow", R.color.Snow))
            colorList.add(ColorNameAndId("SpringGreen", R.color.SpringGreen))
            colorList.add(ColorNameAndId("SteelBlue", R.color.SteelBlue))
            colorList.add(ColorNameAndId("Tan", R.color.Tan))
            colorList.add(ColorNameAndId("Teal", R.color.Teal))
            colorList.add(ColorNameAndId("Thistle", R.color.Thistle))
            colorList.add(ColorNameAndId("Tomato", R.color.Tomato))
            colorList.add(ColorNameAndId("Turquoise", R.color.Turquoise))
            colorList.add(ColorNameAndId("Violet", R.color.Violet))
            colorList.add(ColorNameAndId("Wheat", R.color.Wheat))
            colorList.add(ColorNameAndId("White", R.color.White))
            colorList.add(ColorNameAndId("WhiteSmoke", R.color.WhiteSmoke))
            colorList.add(ColorNameAndId("Yellow", R.color.Yellow))
            colorList.add(ColorNameAndId("YellowGreen", R.color.YellowGreen))
            return colorList
        }

        private fun initBasicColorNameAndIdList(): ArrayList<ColorNameAndId> {
            val colorList = ArrayList<ColorNameAndId>()
            colorList.add(ColorNameAndId("Black", R.color.Black))
            colorList.add(ColorNameAndId("Blue", R.color.Blue))
            colorList.add(ColorNameAndId("Brown", R.color.Brown))
            colorList.add(ColorNameAndId("Gray", R.color.Gray))
            colorList.add(ColorNameAndId("Green", R.color.Green))
            colorList.add(ColorNameAndId("Orange", R.color.Orange))
            colorList.add(ColorNameAndId("Pink", R.color.Pink))
            colorList.add(ColorNameAndId("Purple", R.color.Purple))
            colorList.add(ColorNameAndId("Red", R.color.Red))
            colorList.add(ColorNameAndId("White", R.color.White))
            colorList.add(ColorNameAndId("Yellow", R.color.Yellow))
            return colorList
        }

        fun getAllColorNameAndIdList() = colorNameAndIdList
        fun getAllColorNames(): ArrayList<String>{
            val colorNames = ArrayList<String>()
            for(c in colorNameAndIdList) colorNames.add(c.name)
            return colorNames
        }
        fun getAllColorIds(): ArrayList<Int>{
            val colorIds = ArrayList<Int>()
            for(c in colorNameAndIdList) colorIds.add(c.id)
            return colorIds
        }
        fun getAllColors(context: Context): ArrayList<Int>{
            val colorList = ArrayList<Int>()
            for(c in colorNameAndIdList){
                colorList.add(getColorFromId(context, c.id))
            }
            return colorList
        }
        fun getAllBasicColors(context: Context): ArrayList<Int>{
            val colorList = ArrayList<Int>()
            for(c in basicColorNameAndIdList){
                colorList.add(getColorFromId(context, c.id))
            }
            return colorList
        }

        fun getColorFromId(context: Context, id: Int) = ContextCompat.getColor(context, id)
        fun getColorFromPosition(context: Context, position: Int): Int{
            return getColorFromId(context, colorNameAndIdList[position].id)
        }
        fun getColorFromName(context: Context, name: String): Int?{
            for(c in colorNameAndIdList) if(c.name == name) return getColorFromId(context, c.id)
            return null
        }
        fun getColorNameFromId(id: Int): String{
            for(c in colorNameAndIdList) if(c.id == id) return c.name
            return "Transparent" //;)
        }
        fun getColorFromARGB(a: Int, r: Int, g: Int, b: Int): Int{
            return Color.argb(a, r, g, b)
        }
    }
}
open class ColorNameAndId(var name: String, var id: Int)