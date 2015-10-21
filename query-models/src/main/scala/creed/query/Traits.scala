package creed
package query
package models

import commons.catalogue._, attributes._

import core.nlp.NLP


abstract class Trait(token: Token)

object Traits {
  def apply[From, T <: Trait](from: From)(implicit buildr: TraitBuildr[From, T]) = buildr.build(from)
}

case class ItemTypeGroupTrait(token: Token) extends Trait(token)
case class ClothingStyleTrait(token: Token) extends Trait(token)
case class FabricTrait(token: Token) extends Trait(token)
case class FitTrait(token: Token) extends Trait(token)
case class ColorTrait(token: Token) extends Trait(token)
case class StylingTipsTrait(token: Token) extends Trait(token)
case class DescriptionTrait(token: Token) extends Trait(token)


trait TraitBuildr[From, T <: Trait] {
  def build(from: From): Seq[T]
}

object TraitBuildrs extends TraitBuildrs

trait TraitBuildrs {

  implicit object ItemTypeGroupTraitBuildr extends TraitBuildr[ItemTypeGroup, ItemTypeGroupTrait] {
    def build(group: ItemTypeGroup) = Seq(ItemTypeGroupTrait(group.name))
  }

  implicit object ClothingStyleTraitBuildr extends TraitBuildr[ClothingStyle, ClothingStyleTrait] {
    def build(style: ClothingStyle) = Seq(ClothingStyleTrait(style.name))
  }

  implicit object FabricTraitBuildr extends TraitBuildr[ApparelFabric, FabricTrait] {
    def build(fabric: ApparelFabric) = NLP.nouns(fabric.fabric) map (FabricTrait(_))
  }

  implicit object FitTraitBuildr extends TraitBuildr[ApparelFit, FitTrait] {
    def build(fit: ApparelFit) = NLP.nouns(fit.fit) map (FitTrait(_))
  }

  implicit object ColorTraitBuildr extends TraitBuildr[Colors, ColorTrait] {
    def build(colors: Colors) = colors.values.map(c => ColorTrait(c.toLowerCase))
  }

  implicit object StylingTipsBuildr extends TraitBuildr[StylingTips, StylingTipsTrait] {
    def build(tips: StylingTips) = NLP.nouns(tips.text) map (StylingTipsTrait(_))
  }

  implicit object DescriptionTraitBuildr extends TraitBuildr[Description, DescriptionTrait] {
    def build(descr: Description) = NLP.nouns(descr.text) map (DescriptionTrait(_))
  }

}