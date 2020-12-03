package de.passbutler.desktop.base

import de.passbutler.common.base.RelativeDateFormattingTranslations
import de.passbutler.common.base.UnitTranslation
import de.passbutler.common.base.UnitTranslations
import tornadofx.Component
import tornadofx.get

fun createRelativeDateFormattingTranslations(component: Component): RelativeDateFormattingTranslations {
    return RelativeDateFormattingTranslations(
        unitTranslations = UnitTranslations(
            second = UnitTranslation(component.messages["general_unit_second_one"], component.messages["general_unit_second_other"]),
            minute = UnitTranslation(component.messages["general_unit_minute_one"], component.messages["general_unit_minute_other"]),
            hour = UnitTranslation(component.messages["general_unit_hour_one"], component.messages["general_unit_hour_other"]),
            day = UnitTranslation(component.messages["general_unit_day_one"], component.messages["general_unit_day_other"]),
            month = UnitTranslation(component.messages["general_unit_month_one"], component.messages["general_unit_month_other"]),
            year = UnitTranslation(component.messages["general_unit_year_one"], component.messages["general_unit_year_other"])
        ),
        sinceString = component.messages["general_since"]
    )
}
