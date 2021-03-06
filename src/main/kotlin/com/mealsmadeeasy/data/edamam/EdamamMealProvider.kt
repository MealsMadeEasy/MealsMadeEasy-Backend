package com.mealsmadeeasy.data.edamam

import com.mealsmadeeasy.FirebaseInstance
import com.mealsmadeeasy.data.MealStore
import com.mealsmadeeasy.data.SearchQueryException
import com.mealsmadeeasy.data.edamam.model.DietLabels
import com.mealsmadeeasy.data.edamam.model.EdamamRecipe
import com.mealsmadeeasy.data.edamam.model.HealthLabels
import com.mealsmadeeasy.data.edamam.service.createEdamamApi
import com.mealsmadeeasy.data.mercury.MercuryParser
import com.mealsmadeeasy.model.*
import com.mealsmadeeasy.utils.block
import com.mealsmadeeasy.utils.firstBlocking
import com.mealsmadeeasy.utils.get
import org.jetbrains.ktor.http.HttpStatusCode

object EdamamMealProvider : MealStore.MealProvider {

    private const val ID_PREFIX = "edamam/"
    private val service = CachedEdamam(createEdamamApi())

    // I'm so sorry.
    private val ingredientQuantityRegex = """(([1-9][0-9]*(\.[0-9]+)?\s?)|(([1-9][0-9]*/[1-9][0-9]*|[½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅐⅛⅜⅝⅞⅑⅒])\s?))+(x|lb|tsp|tbsp|cup|oz|g|teaspoon|teaspoons|pound|\s+)[.s]*\s""".toRegex()

    private val enableApiRequests: Boolean
        get() = FirebaseInstance.database["enableEdamam"].firstBlocking() ?: false

    override fun getRandomMeals(count: Int): List<Meal> {
        if (!enableApiRequests) {
            return emptyList()
        }

        return service.search(query = "Tacos")
                .block()
                .hits
                .take(count)
                .map { it.recipe }
                .map { it.toMeal() }
    }

    override fun findMealById(mealId: String): Meal? {
        if (!mealId.startsWith(ID_PREFIX) || !enableApiRequests) {
            return null
        }

        return service.findById(id = mealId.substringAfter(ID_PREFIX))
                .block()
                .firstOrNull()
                ?.toMeal()
    }

    override fun getRecipeForMeal(mealId: String): Recipe? {
        if (!mealId.startsWith(ID_PREFIX) || !enableApiRequests) {
            return null
        }

        return service.findById(id = mealId.substringAfter(ID_PREFIX))
                .block()
                .firstOrNull()
                ?.url
                ?.let {
                    Recipe(MercuryParser.parseWebsite(it).content)
                }
    }

    override fun search(query: String, filters: List<String>): List<Meal> {
        if (filters.any { !it.startsWith(ID_PREFIX) } || !enableApiRequests) {
            return emptyList()
        }

        val edamamFilters = filters.map { it.removePrefix(ID_PREFIX) }
        val dietFilters = DietLabels.values().filter { it.apiParameter in edamamFilters }
        val healthFilters = HealthLabels.values().filter { it.apiParameter in edamamFilters }

        if (dietFilters.size > 1) {
            throw SearchQueryException("Only one diet filter may be applied", HttpStatusCode.BadRequest)
        } else if (healthFilters.size > 1) {
            throw SearchQueryException("Only one health filter may be applied", HttpStatusCode.BadRequest)
        }

        val healthLabel = healthFilters.firstOrNull()
        val dietLabel = dietFilters.firstOrNull()

        return when {
            healthLabel != null && dietLabel != null -> {
                service.search(query = query, healthLabel = healthLabel.apiParameter,
                        dietLabel = dietLabel.apiParameter)
            }
            healthLabel != null -> {
                service.searchHealthFilter(query = query, healthLabel = healthLabel.apiParameter)
            }
            dietLabel != null -> {
                service.searchDietFilter(query = query, dietLabel = dietLabel.apiParameter)
            }
            else -> {
                service.search(query = query)
            }
        }.let { call ->
            call.block()
                    .hits
                    .map { it.recipe }
                    .map { it.toMeal() }
        }
    }

    override fun getAvailableFilters(): List<FilterGroup> {
        if (!enableApiRequests) {
            return emptyList()
        }

        val dietFilters = DietLabels.values()
                .filter { !it.isPremium }
                .map { Filter(it.webLabel, ID_PREFIX + it.apiParameter) }

        val healthFilters = HealthLabels.values()
                .filter { !it.isPremium }
                .map { Filter(it.webLabel, ID_PREFIX + it.apiParameter) }

        return listOf(
                FilterGroup(
                        groupId = ID_PREFIX + "diet-filters",
                        groupName = "Diet",
                        filters = dietFilters.sortedBy { it.name.toLowerCase() },
                        maximumActive = 1
                ),
                FilterGroup(
                        groupId = ID_PREFIX + "health-filters",
                        groupName = "Health",
                        filters = healthFilters.sortedBy { it.name.toLowerCase() },
                        maximumActive = 1
                )
        )
    }

    override fun getIngredients(mealId: String): List<Ingredient>? {
        if (!mealId.startsWith(ID_PREFIX) || !enableApiRequests) {
            return null
        }

        return service.findById(id = mealId.substringAfter(ID_PREFIX))
                .block()
                .firstOrNull()
                ?.ingredients
                ?.map {
                    Ingredient(
                            quantity = it.weight,
                            unit = "grams",
                            name = it.text.toLowerCase()
                                    .replace(""",.*""".toRegex(), "")
                                    .replace("""\(.*\)""".toRegex(), "")
                                    .replace(ingredientQuantityRegex, "")
                                    .replace("""[^a-z\s'%#\-]""", "")
                                    .trim()
                    )
                }
    }

    private fun EdamamRecipe.toMeal(): Meal {
        return Meal(
                id = "$ID_PREFIX$uri",
                name = label,
                description = healthLabels.map { it.webLabel }.joinToString(),
                thumbnailUrl = image
        )
    }

}