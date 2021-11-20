/**
 * Pure Flavor is a catch-all for Actors which only serve to describe fluff and are otherwise non-interactive.
 */
class PureFlavor(
    name: String,
    additionalDescriptionLines: List<String>
) : Actor(
    name = name,
    additionalDescriptionLines = additionalDescriptionLines
)