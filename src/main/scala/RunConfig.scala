// TODO: Add output file configuration

case class RunConfig(
  englishFilename: String,
  japaneseFilename: String,
  engMinFont: Int,
  engMaxFont: Int,
  jpMinFont: Int,
  jpMaxFont: Int,
  resolutionWidth: Int,
  resolutionHeight: Int,
  ppi: Int,
  padding: Int,
  furiganaSpacing: Int,
  lineSpacing: Int,
  typeface: String
)

object RunConfig {
  val logger = GlobalContext.logger
  type OptionMap = Map[Symbol, Any]

  def coerceToInt(a: Any): Int = {
    a match {
      case i: Int => i
      case s: String => s.toInt
      case _ => throw new NumberFormatException
    }
  }

  def coerceTupleToInt(t: Any): (Int, Int) = {
    t match { case (a, b) => (coerceToInt(a), coerceToInt(b)) }
  }

  def gatherRuntimeConfiguration(args: List[String]): RunConfig = {
    logger.debug("Raw Command Line Arguments:\n%s".format(args.toString))
    val options = convertArgumentsToMap(args)
    if(options.contains('jpInfile) && options.contains('engInfile)) {
      val (engMinFont, engMaxFont) = coerceTupleToInt(options.getOrElse('e, Defaults.engFontRange))
      val (jpMinFont, jpMaxFont) = coerceTupleToInt(options.getOrElse('j, Defaults.jpFontRange))
      val (resolutionWidth, resolutionHeight) = coerceTupleToInt(options.getOrElse('r, Defaults.resolution))
      try { RunConfig(
        options('engInfile).asInstanceOf[String],
        options('jpInfile).asInstanceOf[String],
        engMinFont,
        engMaxFont,
        jpMinFont,
        jpMaxFont,
        resolutionWidth,
        resolutionHeight,
        coerceToInt(options.getOrElse('P, Defaults.ppi)),
        coerceToInt(options.getOrElse('p, Defaults.horizontalPadding)),
        coerceToInt(options.getOrElse('F, Defaults.furiganaSpacing)),
        coerceToInt(options.getOrElse('l, Defaults.lineSpacing)),
        options.getOrElse('f, Defaults.fontFamily).asInstanceOf[String]
      ) } catch {
        case nfe: NumberFormatException =>
          logger.error("An error occured while trying to convert an argument to an integer")
          logger.error("Argument map:\n%s".format(options.toString()))
          throw nfe
      }
    } else {
      println(GlobalContext.usage)
      throw new IllegalArgumentException("The input files for the program were not specified!")
    }
  }

  def convertArgumentsToMap(args: List[String]): OptionMap = {
    def gatherFlags(map : OptionMap, list: List[String]) : OptionMap = {
      def isSwitch(s : String) = s(0) == '-'
      list match {
        case Nil => map
        case "-ie" :: engTextFile :: tail =>
          gatherFlags(map ++ Map('engInfile -> engTextFile), tail)
        case "-ij" :: jpTextFile :: tail =>
          gatherFlags(map ++ Map('jpInfile -> jpTextFile), tail)
        case "-e" :: fontMin :: fontMax :: tail =>
          gatherFlags(map ++ Map('e -> (fontMin.toInt, fontMax.toInt)), tail)
        case "-j" :: fontMin :: fontMax :: tail =>
          gatherFlags(map ++ Map('j -> (fontMin.toInt, fontMax.toInt)), tail)
        case "-r" :: resolutionWidth :: resolutionHeight :: tail =>
          gatherFlags(map ++ Map('r -> (resolutionWidth.toInt, resolutionHeight.toInt)), tail)
        case "-p" :: horizontalPadding :: tail =>
          gatherFlags(map ++ Map('p -> horizontalPadding.toInt), tail)
        case "-P" :: ppi :: tail =>
          gatherFlags(map ++ Map('P -> ppi.toInt), tail)
        case "-f" :: fontName :: tail =>
          gatherFlags(map ++ Map('f -> fontName), tail)
        case "-F" :: furiganaSpacing :: tail =>
          gatherFlags(map ++ Map('F -> furiganaSpacing.toInt), tail)
        case "-l" :: lineSpacing :: tail =>
          gatherFlags(map ++ Map('l -> lineSpacing.toInt), tail)
        case engTextFile :: jpTextFile :: opt2 :: tail if isSwitch(opt2) =>
          gatherFlags(map ++ Map('jpInfile -> jpTextFile, 'engInfile -> engTextFile), opt2 :: tail)
        case engTextFile :: jpTextFile :: Nil =>  gatherFlags(map ++ Map('jpInfile -> jpTextFile, 'engInfile -> engTextFile), Nil)
        case option :: tail =>
          logger.error(args.toString)
          throw new IllegalArgumentException("Unknown option "+option)
      }
    }
    gatherFlags(Map(),args)
  }
}

object Defaults {
  private val defaultConf = GlobalContext.conf.getConfig("default")

  val ppi: String = defaultConf.getInt("ppi") + ""
  val resolution: (Int, Int) = (defaultConf.getInt("resolution.width"), defaultConf.getInt("resolution.height"))
  val horizontalPadding: Int = defaultConf.getInt("padding.horizontal")
  val fontFamily: String = defaultConf.getString("font.typeface") + ""
  val engFontRange: (Int, Int) = (defaultConf.getInt("font.eng.min"), defaultConf.getInt("font.eng.max"))
  val jpFontRange: (Int, Int)  = (defaultConf.getInt("font.jp.min"), defaultConf.getInt("font.jp.max"))
  val lineSpacing: Int = defaultConf.getInt("spacing.eng")
  val furiganaSpacing: Int = defaultConf.getInt("spacing.furigana")
}