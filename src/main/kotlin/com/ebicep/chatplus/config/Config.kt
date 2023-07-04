package config

import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import java.io.File

/**
 *
 * @see <a href="https://github.com/EssentialGG/Vigilance/blob/master/src/main/kotlin/gg/essential/vigilance/example/ExampleConfig.kt">Example</a>
 */
object Config : Vigilant(File("./config/ChatPlus.toml")) {
    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Focused Height",
        description = "",
        category = "Test",
    )
    var focusedHeightPercent = 1f // % of height

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Unfocused Height",
        description = "",
        category = "Test",
    )
    var unfocusedHeightPercent = 1f // % of height

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Width",
        description = "",
        category = "Test",
    )
    var widthPercent = 1f // % of width

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Scale",
        description = "",
        category = "Test",
    )
    var scale = 1f

    init {
        initialize()

        val clazz = javaClass
//        registerListener(clazz.getDeclaredField("colorWithAlpha")) { color: Color ->
//            UChat.chat("colorWithAlpha listener activated! New color: $color")
//        }

//        addDependency(clazz.getDeclaredField("dependant"), clazz.getDeclaredField("dependency"))
//        addDependency(clazz.getDeclaredField("propertyPete"), clazz.getDeclaredField("toggleTom"))
//        addDependency(clazz.getDeclaredField("checkboxChuck"), clazz.getDeclaredField("toggleTom"))
//        addDependency("valueDependant", "selectorDependency") { value: Int -> value == 0 }
//        addDependency("inverted", "dependency") { value: Boolean -> !value }
//        setCategoryDescription(
//            "Property Overview",
//            "This category is a quick overview of all of the components. For a deep-dive into the component, check their specific subcategories."
//        )
//
//        setCategoryDescription(
//            "Property Deep-Dive",
//            "This category will go in depth into every component, and show off some of the customization options available in Vigilance. It contains a subcategory for every single property type available."
//        )
//
//        setSubcategoryDescription(
//            "Property Deep-Dive",
//            "Buttons",
//            "Buttons are a great way for the user to run an action. Buttons don't have any associated state, and as such their annotation target has to be a method."
//        )
    }
}