/*
 * Copyright 2019-2022 Foreseeti AB <https://foreseeti.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mal_lang.lib;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.mal_lang.langspec.AttackStepType;
import org.mal_lang.langspec.Lang;
import org.mal_lang.langspec.Multiplicity;
import org.mal_lang.langspec.Risk;
import org.mal_lang.langspec.builders.AssetBuilder;
import org.mal_lang.langspec.builders.AssociationBuilder;
import org.mal_lang.langspec.builders.AttackStepBuilder;
import org.mal_lang.langspec.builders.CategoryBuilder;
import org.mal_lang.langspec.builders.LangBuilder;
import org.mal_lang.langspec.builders.StepsBuilder;
import org.mal_lang.langspec.builders.VariableBuilder;
import org.mal_lang.langspec.builders.step.StepAttackStepBuilder;
import org.mal_lang.langspec.builders.step.StepCollectBuilder;
import org.mal_lang.langspec.builders.step.StepDifferenceBuilder;
import org.mal_lang.langspec.builders.step.StepExpressionBuilder;
import org.mal_lang.langspec.builders.step.StepFieldBuilder;
import org.mal_lang.langspec.builders.step.StepIntersectionBuilder;
import org.mal_lang.langspec.builders.step.StepSubTypeBuilder;
import org.mal_lang.langspec.builders.step.StepTransitiveBuilder;
import org.mal_lang.langspec.builders.step.StepUnionBuilder;
import org.mal_lang.langspec.builders.step.StepVariableBuilder;
import org.mal_lang.langspec.ttc.TtcAddition;
import org.mal_lang.langspec.ttc.TtcDistribution;
import org.mal_lang.langspec.ttc.TtcDivision;
import org.mal_lang.langspec.ttc.TtcExponentiation;
import org.mal_lang.langspec.ttc.TtcExpression;
import org.mal_lang.langspec.ttc.TtcFunction;
import org.mal_lang.langspec.ttc.TtcMultiplication;
import org.mal_lang.langspec.ttc.TtcNumber;
import org.mal_lang.langspec.ttc.TtcSubtraction;

/**
 * Class for converting {@link org.mal_lang.lib.AST} objects into {@link org.mal_lang.langspec.Lang}
 * objects.
 *
 * @since 0.1.0
 */
public class LangConverter {
  private final MalLogger LOGGER;
  private final Map<String, List<AST.Category>> astCategories = new LinkedHashMap<>();
  private final List<AST.Association> astAssociations = new ArrayList<>();
  private final Map<String, String> astDefines = new LinkedHashMap<>();
  private final Map<String, byte[]> svgIcons;
  private final Map<String, byte[]> pngIcons;
  private final String license;
  private final String notice;

  private LangConverter(
      AST ast,
      boolean verbose,
      boolean debug,
      Map<String, byte[]> svgIcons,
      Map<String, byte[]> pngIcons,
      String license,
      String notice) {
    Locale.setDefault(Locale.ROOT);
    this.LOGGER = new MalLogger("LANG_CONVERTER", verbose, debug);

    // Collect defines
    for (var astDefine : ast.getDefines()) {
      this.astDefines.put(astDefine.key.id, astDefine.value);
    }

    // Collect categories
    var allAstCategories = ast.getCategories();
    for (var astCategory : allAstCategories) {
      if (!this.astCategories.containsKey(astCategory.name.id)) {
        this.astCategories.put(astCategory.name.id, new ArrayList<>());
      }
      this.astCategories.get(astCategory.name.id).add(astCategory);
    }

    // Collect associations
    for (var astAssociation : ast.getAssociations()) {
      this.astAssociations.add(astAssociation);
    }

    this.svgIcons = svgIcons == null ? Map.of() : Map.copyOf(svgIcons);
    this.pngIcons = pngIcons == null ? Map.of() : Map.copyOf(pngIcons);
    this.license = license;
    this.notice = notice;
  }

  /**
   * Converts an {@link org.mal_lang.lib.AST} object into a {@link org.mal_lang.langspec.Lang}
   * object.
   *
   * @param ast the {@link org.mal_lang.lib.AST} to convert
   * @return a {@link org.mal_lang.langspec.Lang}
   * @since 0.1.0
   */
  public static Lang convert(AST ast) {
    return convert(ast, false, false, null, null, null, null);
  }

  /**
   * Converts an {@link org.mal_lang.lib.AST} object into a {@link org.mal_lang.langspec.Lang}
   * object.
   *
   * @param ast the {@link org.mal_lang.lib.AST} to convert
   * @param verbose whether verbose information should be logged
   * @param debug whether debug information should be logged
   * @return a {@link org.mal_lang.langspec.Lang}
   * @since 0.1.0
   */
  public static Lang convert(AST ast, boolean verbose, boolean debug) {
    return convert(ast, verbose, debug, null, null, null, null);
  }

  /**
   * Converts an {@link org.mal_lang.lib.AST} object into a {@link org.mal_lang.langspec.Lang}
   * object.
   *
   * @param ast the {@link org.mal_lang.lib.AST} to convert
   * @param svgIcons the SVG icons of the language, or {@code null}
   * @param pngIcons the PNG icons of the language, or {@code null}
   * @param license the license of the language, or {@code null}
   * @param notice the notice of the language, or {@code null}
   * @return a {@link org.mal_lang.langspec.Lang}
   * @since 0.1.0
   */
  public static Lang convert(
      AST ast,
      Map<String, byte[]> svgIcons,
      Map<String, byte[]> pngIcons,
      String license,
      String notice) {
    return convert(ast, false, false, svgIcons, pngIcons, license, notice);
  }

  /**
   * Converts an {@link org.mal_lang.lib.AST} object into a {@link org.mal_lang.langspec.Lang}
   * object.
   *
   * @param ast the {@link org.mal_lang.lib.AST} to convert
   * @param verbose whether verbose information should be logged
   * @param debug whether debug information should be logged
   * @param svgIcons the SVG icons of the language, or {@code null}
   * @param pngIcons the PNG icons of the language, or {@code null}
   * @param license the license of the language, or {@code null}
   * @param notice the notice of the language, or {@code null}
   * @return a {@link org.mal_lang.langspec.Lang}
   * @since 0.1.0
   */
  public static Lang convert(
      AST ast,
      boolean verbose,
      boolean debug,
      Map<String, byte[]> svgIcons,
      Map<String, byte[]> pngIcons,
      String license,
      String notice) {
    return new LangConverter(ast, verbose, debug, svgIcons, pngIcons, license, notice).convertLog();
  }

  private Lang convertLog() {
    var lang = this.convertLang();
    LOGGER.print();
    return lang;
  }

  private Lang convertLang() {
    var langBuilder = new LangBuilder();

    for (var entry : this.astDefines.entrySet()) {
      langBuilder.addDefine(entry.getKey(), entry.getValue());
    }

    for (var entry : this.astCategories.entrySet()) {
      var categoryBuilder = new CategoryBuilder(entry.getKey());
      for (var astCategory : entry.getValue()) {
        for (var meta : astCategory.meta) {
          categoryBuilder.getMeta().addEntry(meta.type.id, meta.string);
        }
      }
      langBuilder.addCategory(categoryBuilder);
    }

    for (var astCategories : this.astCategories.values()) {
      for (var astCategory : astCategories) {
        for (var astAsset : astCategory.assets) {
          var assetBuilder =
              new AssetBuilder(
                  astAsset.name.id,
                  astCategory.name.id,
                  astAsset.isAbstract,
                  astAsset.parent.isPresent() ? astAsset.parent.get().id : null);
          for (var meta : astAsset.meta) {
            assetBuilder.getMeta().addEntry(meta.type.id, meta.string);
          }
          for (var astVariable : astAsset.variables) {
            var variableBuilder =
                new VariableBuilder(
                    astVariable.name.id, this.convertStepExpression(astVariable.expr, false));
            assetBuilder.addVariable(variableBuilder);
          }
          for (var astAttackStep : astAsset.attackSteps) {
            var name = astAttackStep.name.id;
            var type = this.convertAttackStepType(astAttackStep.type);
            var risk =
                astAttackStep.cia.isPresent() ? this.convertRisk(astAttackStep.cia.get()) : null;
            var ttc =
                astAttackStep.ttc.isPresent()
                    ? this.convertTtcExpression(astAttackStep.ttc.get())
                    : null;
            var requires =
                astAttackStep.requires.isPresent()
                    ? this.convertRequires(astAttackStep.requires.get())
                    : null;
            var reaches =
                astAttackStep.reaches.isPresent()
                    ? this.convertReaches(astAttackStep.reaches.get())
                    : null;
            var attackStepBuilder = new AttackStepBuilder(name, type, risk, ttc, requires, reaches);
            for (var meta : astAttackStep.meta) {
              attackStepBuilder.getMeta().addEntry(meta.type.id, meta.string);
            }
            for (var tag : astAttackStep.tags) {
              attackStepBuilder.addTag(tag.id);
            }
            assetBuilder.addAttackStep(attackStepBuilder);
          }
          if (this.svgIcons.containsKey(assetBuilder.getName())) {
            assetBuilder.setSvgIcon(this.svgIcons.get(assetBuilder.getName()));
          }
          if (this.pngIcons.containsKey(assetBuilder.getName())) {
            assetBuilder.setPngIcon(this.pngIcons.get(assetBuilder.getName()));
          }
          langBuilder.addAsset(assetBuilder);
        }
      }
    }

    if (!this.svgIcons.isEmpty() || !this.pngIcons.isEmpty()) {
      for (var assetBuilder : langBuilder.getAssets()) {
        if (!assetBuilder.isAbstract() && !LangConverter.assetHasIcon(langBuilder, assetBuilder)) {
          LOGGER.warning(String.format("No icon found for asset '%s'", assetBuilder.getName()));
        }
      }
    }

    for (var astAssociation : this.astAssociations) {
      var associationBuilder =
          new AssociationBuilder(
              astAssociation.linkName.id,
              astAssociation.leftAsset.id,
              astAssociation.leftField.id,
              this.convertMultiplicity(astAssociation.leftMult),
              astAssociation.rightAsset.id,
              astAssociation.rightField.id,
              this.convertMultiplicity(astAssociation.rightMult));
      for (var meta : astAssociation.meta) {
        associationBuilder.getMeta().addEntry(meta.type.id, meta.string);
      }
      langBuilder.addAssociation(associationBuilder);
    }

    if (this.license != null) {
      langBuilder.setLicense(this.license);
    }

    if (this.notice != null) {
      langBuilder.setNotice(this.notice);
    }

    return Lang.fromBuilder(langBuilder);
  }

  private AttackStepType convertAttackStepType(AST.AttackStepType astType) {
    switch (astType) {
      case ANY:
        return AttackStepType.OR;
      case ALL:
        return AttackStepType.AND;
      case DEFENSE:
        return AttackStepType.DEFENSE;
      case EXIST:
        return AttackStepType.EXIST;
      case NOTEXIST:
        return AttackStepType.NOT_EXIST;
      default:
        throw new RuntimeException(String.format("Invalid attack step type %s", astType));
    }
  }

  private Risk convertRisk(List<AST.CIA> astCiaList) {
    boolean isConfidentiality = false;
    boolean isIntegrity = false;
    boolean isAvailability = false;
    for (var astCia : astCiaList) {
      switch (astCia) {
        case C:
          isConfidentiality = true;
          break;
        case I:
          isIntegrity = true;
          break;
        case A:
          isAvailability = true;
          break;
      }
    }
    return new Risk(isConfidentiality, isIntegrity, isAvailability);
  }

  private TtcExpression convertTtcExpression(AST.TTCExpr astTtcExpression) {
    if (astTtcExpression instanceof AST.TTCAddExpr) {
      var astTtcAddition = (AST.TTCAddExpr) astTtcExpression;
      return new TtcAddition(
          this.convertTtcExpression(astTtcAddition.lhs),
          this.convertTtcExpression(astTtcAddition.rhs));
    } else if (astTtcExpression instanceof AST.TTCSubExpr) {
      var astTtcSubtraction = (AST.TTCSubExpr) astTtcExpression;
      return new TtcSubtraction(
          this.convertTtcExpression(astTtcSubtraction.lhs),
          this.convertTtcExpression(astTtcSubtraction.rhs));
    } else if (astTtcExpression instanceof AST.TTCMulExpr) {
      var astTtcMultiplication = (AST.TTCMulExpr) astTtcExpression;
      return new TtcMultiplication(
          this.convertTtcExpression(astTtcMultiplication.lhs),
          this.convertTtcExpression(astTtcMultiplication.rhs));
    } else if (astTtcExpression instanceof AST.TTCDivExpr) {
      var astTtcDivision = (AST.TTCDivExpr) astTtcExpression;
      return new TtcDivision(
          this.convertTtcExpression(astTtcDivision.lhs),
          this.convertTtcExpression(astTtcDivision.rhs));
    } else if (astTtcExpression instanceof AST.TTCPowExpr) {
      var astTtcExponentiation = (AST.TTCPowExpr) astTtcExpression;
      return new TtcExponentiation(
          this.convertTtcExpression(astTtcExponentiation.lhs),
          this.convertTtcExpression(astTtcExponentiation.rhs));
    } else if (astTtcExpression instanceof AST.TTCFuncExpr) {
      var astTtcFunction = (AST.TTCFuncExpr) astTtcExpression;
      return new TtcFunction(
          TtcDistribution.fromString(astTtcFunction.name.id),
          astTtcFunction.params.stream().mapToDouble(x -> x).toArray());
    } else if (astTtcExpression instanceof AST.TTCNumExpr) {
      var astTtcNumber = (AST.TTCNumExpr) astTtcExpression;
      return new TtcNumber(astTtcNumber.value);
    } else {
      throw new RuntimeException(
          String.format("Invalid TTC expression type %s", astTtcExpression.getClass().getName()));
    }
  }

  private StepsBuilder convertRequires(AST.Requires astRequires) {
    var stepsBuilder = new StepsBuilder(true);
    for (var astStepExpression : astRequires.requires) {
      stepsBuilder.addStepExpression(this.convertStepExpression(astStepExpression, false));
    }
    return stepsBuilder;
  }

  private StepsBuilder convertReaches(AST.Reaches astReaches) {
    var stepsBuilder = new StepsBuilder(!astReaches.inherits);
    for (var astStepExpression : astReaches.reaches) {
      stepsBuilder.addStepExpression(this.convertStepExpression(astStepExpression, true));
    }
    return stepsBuilder;
  }

  private StepExpressionBuilder convertStepExpression(AST.Expr astExpr, boolean isAttackStep) {
    if (astExpr instanceof AST.UnionExpr) {
      var astStepUnion = (AST.UnionExpr) astExpr;
      return new StepUnionBuilder(
          this.convertStepExpression(astStepUnion.lhs, false),
          this.convertStepExpression(astStepUnion.rhs, isAttackStep));
    } else if (astExpr instanceof AST.IntersectionExpr) {
      var astStepIntersection = (AST.IntersectionExpr) astExpr;
      return new StepIntersectionBuilder(
          this.convertStepExpression(astStepIntersection.lhs, false),
          this.convertStepExpression(astStepIntersection.rhs, isAttackStep));
    } else if (astExpr instanceof AST.DifferenceExpr) {
      var astStepDifference = (AST.DifferenceExpr) astExpr;
      return new StepDifferenceBuilder(
          this.convertStepExpression(astStepDifference.lhs, false),
          this.convertStepExpression(astStepDifference.rhs, isAttackStep));
    } else if (astExpr instanceof AST.StepExpr) {
      var astStepCollect = (AST.StepExpr) astExpr;
      return new StepCollectBuilder(
          this.convertStepExpression(astStepCollect.lhs, false),
          this.convertStepExpression(astStepCollect.rhs, isAttackStep));
    } else if (astExpr instanceof AST.TransitiveExpr) {
      var astStepTransitive = (AST.TransitiveExpr) astExpr;
      return new StepTransitiveBuilder(this.convertStepExpression(astStepTransitive.e, false));
    } else if (astExpr instanceof AST.SubTypeExpr) {
      var astStepSubType = (AST.SubTypeExpr) astExpr;
      return new StepSubTypeBuilder(
          astStepSubType.subType.id, this.convertStepExpression(astStepSubType.e, false));
    } else if (astExpr instanceof AST.IDExpr) {
      var astStepId = (AST.IDExpr) astExpr;
      if (isAttackStep) {
        return new StepAttackStepBuilder(astStepId.id.id);
      } else {
        return new StepFieldBuilder(astStepId.id.id);
      }
    } else if (astExpr instanceof AST.CallExpr) {
      var astStepVariable = (AST.CallExpr) astExpr;
      return new StepVariableBuilder(astStepVariable.id.id);
    } else {
      throw new RuntimeException(
          String.format("Invalid step expression type %s", astExpr.getClass().getName()));
    }
  }

  private Multiplicity convertMultiplicity(AST.Multiplicity astMultiplicity) {
    switch (astMultiplicity) {
      case ZERO_OR_ONE:
        return Multiplicity.ZERO_OR_ONE;
      case ZERO_OR_MORE:
        return Multiplicity.ZERO_OR_MORE;
      case ONE:
        return Multiplicity.ONE;
      case ONE_OR_MORE:
        return Multiplicity.ONE_OR_MORE;
      default:
        throw new RuntimeException(String.format("Invalid multiplicity %s", astMultiplicity));
    }
  }

  private static boolean assetHasIcon(LangBuilder langBuilder, AssetBuilder assetBuilder) {
    if (assetBuilder.getSvgIcon() != null || assetBuilder.getPngIcon() != null) {
      return true;
    }
    if (assetBuilder.getSuperAsset() == null) {
      return false;
    }
    for (var superAssetBuilder : langBuilder.getAssets()) {
      if (superAssetBuilder.getName().equals(assetBuilder.getSuperAsset())) {
        return LangConverter.assetHasIcon(langBuilder, superAssetBuilder);
      }
    }
    throw new IllegalStateException();
  }
}
