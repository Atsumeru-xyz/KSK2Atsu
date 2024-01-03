package xyz.atsumeru.ksk2atsu.utils;

import xyz.atsumeru.ksk2atsu.App;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small collection of Comic utils
 */
public class ComicUtils {
    private static final Pattern AUTUMN = Pattern.compile("(.*) (Autumn \\d+.*)");
    private static final Pattern SPRING = Pattern.compile("(.*) (Spring \\d+.*)");
    private static final Pattern DIGITS = Pattern.compile("(.*) (\\d+.*)");
    private static final Pattern VOL = Pattern.compile("(.*) (Vol\\.\\d+)");
    private static final Pattern VOL_SPACE = Pattern.compile("(.*) (Vol\\. \\d+)");
    private static final Pattern SHARP = Pattern.compile("(.*) (#\\d+)");

    public static final List<Pattern> PATTERNS = new ArrayList<>() {{
        add(AUTUMN);
        add(SPRING);
        add(DIGITS);
        add(VOL);
        add(VOL_SPACE);
        add(SHARP);
    }};

    /**
     * Returns magazine parsing {@link Pattern} from {@link #PATTERNS} by index. Used for recursive name parsing
     *
     * @param patternIndex {@link #PATTERNS} index
     * @return {@link Pattern} by index
     */
    public static Pattern getPattern(int patternIndex) {
        try {
            return PATTERNS.get(patternIndex);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Recursively detect Comic magazine and Issue {@link Pair} from given {@link String} name
     *
     * @param magazine     {@link String} name
     * @param patternIndex {@link #PATTERNS} index for recursive parsing
     * @return {@link Pair} of {@link String} Comic Magazine and {@link String} Comic Issue
     */
    public static Pair<String, String> detectComicNameAndIssue(String magazine, int patternIndex) {
        // Get pattern by index
        Pattern pattern = getPattern(patternIndex);
        if (pattern == null) {
            return null;
        }

        if (StringUtils.isNotEmpty(magazine)) {
            Matcher matcher = pattern.matcher(magazine);
            if (matcher.find()) {
                // Comic name
                String comicName = matcher.group(1);
                // Comic issue
                String comicIssue = matcher.group(2);

                return new Pair<>(comicName, comicIssue);
            } else {
                // Recursively go through all the patterns and try to identify the name and issue of Comic
                return detectComicNameAndIssue(magazine, ++patternIndex);
            }
        } else {
            return null;
        }
    }

    /**
     * Merge Comic Magazine name with Issue depending on Issue type (Year, Volume, #)
     *
     * @param comicName  {@link String} Comic Magazine name
     * @param comicIssue {@link String} Comic Issue
     * @return merged Comic Magazine name with Issue
     */
    public static String getComicWithIssueName(String comicName, String comicIssue) {
        if (comicIssue.startsWith("#")) {
            return String.format("%s %s", comicName, comicIssue);
        } else if (comicIssue.toLowerCase().contains("vol")) {
            Integer volumeNumber = Integer.valueOf(comicIssue.replace("Vol.", "").trim());
            return String.format("%s - Volume %s", comicName, String.format("%02d", volumeNumber));
        } else {
            return String.format("%s [%s]", comicName, comicIssue);
        }
    }

    /**
     * Remove all non-alphabetic, non-numerical and Space symbols in given {@link String}
     *
     * @param title input {@link String}
     * @return cleaned {@link String}
     */
    public static String removeNonAlphanumericalSymbols(String title) {
        return title.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll(" +", " ").trim();
    }

    /**
     * Replace denied Windows FS symbols for special Japanese analogs in given {@link String} that may be used for file naming
     *
     * @param title input {@link String}
     * @return safe Windows FS filename
     */
    public static String getTitleReplacedDeniedSymbols(String title) {
        return title.replace(":", "：")
                .replace("?", "？")
                .replace("!", "！")
                .replace("|", "｜")
                .replace("*", "＊")
                .replace("/", "・")
                .replace(">", "＞")
                .replace("\\", "・")
                .replace("...", "…")
                .replaceAll("\\.$", "。")
                .replaceAll("(\"(.*?)\")", "「$2」");
    }

    /**
     * Replace Japanese analogs for denied Windows FS symbols and fix some rare name errors in given {@link String}
     * that may be used for file naming
     *
     * @param title input {@link String}
     * @return unsafe Windows FS filename with some individual fixes
     */
    public static String getCleanedTitle(String title) {
        return title.replaceAll("\\[.*?]", "")
                .trim()
                .replace("꞉", ":")
                .replace("：", ":")
                .replace("？", "?")
                .replace("！", "!")
                .replace("｜", "|")
                .replace("＊", "*")
                .replace("・", "/")
                .replace("＞", ">")
                .replace("・", "\\")
                .replaceAll("。", ".")
                // Individual fixes
                .replace("⁄", "/")
                .replaceAll("^first bareback", "[virgin loss!?] first bareback")
                .replaceAll("in the name of ?love?", "in the name of \"love\"");
    }

    /**
     * Replace denied Windows FS symbols for special Japanese analogs in given artist {@link String} that may be used for file naming
     *
     * @param artist input {@link String}
     * @return safe Windows FS filename
     */
    public static String getArtistReplacedDeniedSymbols(String artist) {
        return artist.replace(":", "：")
                .replace("?", "？")
                .replace("!", "！")
                .replace("|", "｜")
                .replace("*", "＊")
                .replace("...", "…");
    }

    /**
     * Replace denied Windows FS symbols for special Japanese analogs in given title and artist {@link String} that may
     * be used for file naming and them merge them by formula <b>[artist] title</b>
     *
     * @param title  input {@link String}
     * @param artist input {@link String}
     * @return safe Windows FS filename
     */
    public static String getTitleWithArtistWithoutDeniedSymbols(String title, String artist) {
        return removeNonAlphanumericalSymbols(String.format("%s %s", artist, title));
    }

    /**
     * Remove denied Windows FS symbols and author in given title with author {@link String} that may be used for
     * file naming with additional name cleaning from square, round, curly brackets and some predefined "trash"
     *
     * @param title input {@link String}
     * @return safe Windows FS filename
     */
    public static String getCleanTitleWithoutAuthor(String title) {
        return getCleanTitleWithAuthor(title.replaceAll("^(\\[.*?])", ""));
    }

    /**
     * Remove denied Windows FS symbols in given title with author {@link String} that may be used for file naming
     * with additional name cleaning from square, round, curly brackets and some predefined "trash"
     *
     * @param title input {@link String}
     * @return safe Windows FS filename
     */
    public static String getCleanTitleWithAuthor(String title) {
        return removeNonAlphanumericalSymbols(
                title.toLowerCase()
                        .replace(".cbz", "")
                        .replaceAll("^\\(.*?\\) ", "")
                        .replaceAll("\\[.*?\\((.*?)\\)]", "[$1]")
                        .replaceAll("\\([^()]*\\)(?!.*?\\([^()]*\\))$", "")
                        .trim()
                        // Replace some other trash from names
                        .replace("[digital]", "")
                        .replace("[decensored]", "")
                        .replace("[english]", "")
                        .replace("[2d market]", "")
                        .replace("[2d-market]", "")
                        .replace("[2d-market.com]", "")
                        .replace("[irodori comics]", "")
                        .replace("[fakku irodori comics]", "")
                        .replace("[fakku & irodori comics]", "")
                        .replace("[not fakku]", "")
                        .replace("[fakku]", "")
                        .replace("fakku]", "")
                        .replace("[png]", "")
                        .replace("[]", "")
                        .replace("[]", "")
                        .replace("(x1518)", "")
                        .replace("(x1920)", "")
                        .replace("(1920x)", "")
                        .replace("(x2000)", "")
                        .replace("(2560x)", "")
                        .replace("(x2600)", "")
                        .replace("(x2880)", "")
                        .replace("(x3038)", "")
                        .replace("(x3100)", "")
                        .replace("(x3100+)", "")
                        .replace("(x3199)", "")
                        .replace("(x3200)", "")
                        .replace("(x3200-improper)", "")
                        .replace("(png)", "")
                        .replace("(fakku)", "")
                        .replace("(full color version)", "")
                        .replace("{2d-market.com}", "")
                        .replace("x3200 fakku", "")
                        .replace("x3200", "")
                        .trim()
                        // Replace last square brackets multiple times
                        .replaceAll("\\[[^]]*]+$", "")
                        .replaceAll("\\[[^]]*]+$", "")
                        .replaceAll("\\[[^]]*]+$", "")
                        .replaceAll("\\[[^]]*]+$", "")
                        .replaceAll("\\[[^]]*]+$", "")
                        .trim()
                        // Replace last round brackets
                        .replaceAll("\\([^]]*\\)+$", "")
                        .trim()
        );
    }

    /**
     * Fix some known magazine url problems
     *
     * @param magazineUrl input magazine url {@link String}
     * @return fixed {@link String}
     */
    public static String fixMagazineUrl(String magazineUrl) {
        return magazineUrl.replace("Comic-Happining-Vol-0", "Comic-Happining-Vol")
                .replace("Comic-Happining-Volume-0", "Comic-Happining-Vol")
                .replace("Isekairakuten-Volume-0", "Isekairakuten-Vol")
                .replace("Isekairakuten-Volume-", "Isekairakuten-Vol")
                .replace("Europa-Vol-0", "Europa-Vol0")
                .replace("Dascomi-Vol-0", "Dascomi-Vol0")
                .replace("Vol-0", "Vol")
                .replace("Vol-", "Vol")
                .toLowerCase()
                .replace("comic-x-eros-comic-x-eros", "comic-x-eros")
                .replace("dascomi-volume-", "dascomi-vol")
                .replace("comic-koh-volume-0", "comic-koh-vol")
                .replace("girls-form-volume-0", "girls-form-vol")
                .replace("girls-form-volume-", "girls-form-vol")
                .replace("comic-europa-volume-", "comic-europa-vol");
    }

    /**
     * Fix some known cover url problems
     *
     * @param coverUrl input cover url {@link String}
     * @return fixed {@link String}
     */
    public static String fixCoverUrl(String coverUrl) {
        return coverUrl.replace("/omic-X-Eros-83.jpg", "/Comic-X-Eros-83.jpg")
                .replace("..jpg", ".jpg")
                .replace("Isekairakuten-Vol-1.jpg", "Isekairakuten-Vol-1-thumb.jpg")
                .replace("Comic-Kairakuten-2023-04.png", "Comic-Kairakuten-2023-04-thumb.png");
    }

    /**
     * Fix some known magazine issue title problems
     *
     * @param title input magazine issue title {@link String}
     * @return fixed {@link String}
     */
    public static String fixKnowMagazineIssueTitleIssues(String title) {
        if (title.startsWith("eromangal returns")) {
            return "eromangal returns ♥";
        } else if (title.startsWith("the nights to come")) {
            return "the night to come";
        } else if (title.startsWith("invader")) {
            return "invader ♂€";
        } else if (title.startsWith("teacher at bottom-level middle")) {
            return "[hot topic] teacher at bottom-level middle school blackmails a student into a life of non-stop sex";
        } else if (title.startsWith("impoverished sugar baby")) {
            return "[overconfident] impoverished sugar baby college girl & the three old dudes";
        } else if (title.startsWith("disposable runaway girl")) {
            return "[penetration commemoration] disposable runaway girl sent to a disgusting old dude";
        } else if (title.startsWith("96")) {
            return "96 [black]";
        } else if (title.startsWith("when the old sperm")) {
            return "[sad news] when the old sperm donor gets going, it becomes all the rage on social media";
        } else if (title.startsWith("72")) {
            return "72 [summer]";
        } else if (title.startsWith("nagase tooru's")) {
            return "nagase tooru's (♂€) ero manga-like life ~finale~";
        }

        return title;
    }

    /**
     * Fix some known title problems
     *
     * @param title input title {@link String}
     * @return fixed {@link String}
     */
    public static String fixKnownTitleIssues(String title) {
        if (title.equals("navier haruka 2t succubus delivery")) {
            return "navier haruka 2t succubusdelivery my report on the time i called a pair of succubus call girls";
        }

        return title.replace("mutya mako chans development diary", "mutya makochans development diary")
                .replace("chiyami i love my admiral 1", "chiyami i love my admiral")
                .replace("do you like naughty older girls", "do you like naughty older girls 1")
                .replace("ryunosuke endless orgasms tentacle world", "ryunosuke endless orgasm tentacle world")
                .replace("murasakiiro orange rabbitch fuck", "murasakiiro orange rabbitchuck")
                .replace("nekoi hikaru factory of nekoi 02 danzou to tamamo no soapland", "nekoi hikaru factory of nekoi 02 danzo tamamo soapland")
                .replace("otoo evil at heart 1 and 2", "otoo evil at heart 1 2")
                .replace("tsumetoro naughty foxy 11", "tsumetoro naughty foxy vol11")
                .replace("awayume suzuya cosplay report", "awayume suzuya cosplay resort")
                .replace("bangyou butchau shindo l super panpanronpa 1 2", "bangyou butchau shindo l super panpanronpa 12")
                .replace("aiue oka hypnotic sexual counseling 25 followup", "aiue oka hypnotic sexual counseling 25")
                .replace("anzayuu sweet life in another world are you into an older elf ladys friend 2", "anzayuu sweet life in another world 2 are you into an older elf ladys friend")
                .replace("anzayuu sweet life in another world are you into an older elf lady", "anzayuu sweet life in another world 1 are you into an older elf lady")
                .replace("ayakase chiyoko the reason i shoplifted", "ayakase chiyoko the reason why i shoplifted")
                .replace("kogaku kazuya an saya banglissimo", "kogaku kazuya ansaya banglissimo")
                .replace("kogaku kazuya makichans first time with nicochan", "kogaku kazuya makichans first time with nicochan futa ver")
                .replace("doku denpa the princessi is a slut", "doku denpa the princess is a slut")
                .replace("homunculus courting etranger", "homunculus courting tranger")
                .replace("ichinomiya yuu corrupt check up", "ichinomiya yuu corrupt checkup")
                .replace("kitazato nawoki dance with the devil 1", "kitazato nawoki dance with the devil")
                .replace("syomu my meat brings all the gyarus to the yard fakku", "syomu my meat brings all the gyarus to the yard")
                .replace("marui maru cherrygals", "marui maru cherry gals")
                .replace("prime erinas secret recipe vol 2", "prime secret recipe vol 2")
                .replace("takeda aranobu the perverted virgin public morals committee members secret naughty request 3", "takeda aranobu the perverted anal virgin public morals committee members secret naughty request 3")
                .replace("takeda hiromitsu ima real", "takeda hiromitsu imareal")
                .replace("toyo traditional job of washing girls body volume 03", "toyo traditional job of washing girls body volume 3")
                .replace("utu half ripe cherry", "utu halfripe cherry")
                .replace("watanuki ron trauma sex clinic", "watanuki ron trauma sex clinic 1")
                .replace("nemui neru would you allow us to serve you masterbutao", "nemui neru would you allow us to serve you master butao")
                .replace("ayakawa riku eyeing the hot elf in another world 1", "ayakawa riku eyeing the hot elf in another world")
                .replace("akari blast the hostess of this esteemed hot springs is a shameless sex addict", "akari blast the hostess of this esteemed hot springs is a shameless sex addict chapter 1")
                .replace("akatsuki myuuto handson draining with three succubus sisters ch 1", "akatsuki myuuto handson draining with three succubus sisters chapter 1 lamys secret")
                .replace("chilt sakurai 01 that time my own pupil schooled me on life", "chilt sakurai01 that time my own pupil schooled me on life")
                .replace("arai kei knock up game", "arai kei knockup game")
                .replace("ayakase chiyoko 3 vs 1 volleyball match 5 new years match", "ayakase chiyoko 3 vs 1 volleyball match 5 new years game")
                .replace("ayakase chiyoko do you like slutty gyarus", "ayakase chiyoko do you like slutty gyarus 1")
                .replace("ayakawa riku eyeing the hot elf in another world 1", "ayakawa riku eyeing the hot elf in another world")
                .replace("ayane acting up an actress manager lust story", "ayane acting up an actress x manager lust story")
                .replace("bambi cum dump duty excursion", "bambi cump dump duty excursion")
                .replace("ashizuki the biggest loser", "ashizuki the biggest loser winner")
                .replace("doku denpa loveydovey lickie sticky sex with babyfaced teacher", "doku denpa loveydovey lickie sticky sex with babyfaced teacher all to myself")
                .replace("shindou mating with oni 7 banquet chapter", "shindou mating with oni banquet chapter")
                .replace("kurosugatari my gfs mom has got it goin on dream", "kurosu gatari my gfs mom has got it goin on 1")
                .replace("fujisaki chiro my relationship with mrs fujita 1", "fujisaki chiro my relationship with mrs fujita")
                .replace("fukuyama naoto no one does it like you brother 3", "fukuyama naoto sasuoni no one does it like you brother 3")
                .replace("fukuyama naoto no one does it like you brother 4", "fukuyama naoto sasuoni no one does it like you brother 4")
                .replace("fukuyama naoto no one does it like you brother", "fukuyama naoto sasuoni no one does it like you brother 1")
                .replace("higefurai my fabulous fuck day with my amazing mommy 1", "higefurai my fabulous fuck day with my amazing mommy")
                .replace("ind kary choko livestream accident", "ind kary choko 1 livestream accident")
                .replace("ginyou haru sex smartphone 1", "ginyou haru sex smart phone 1")
                .replace("ginyou haru sex smartphone 2", "ginyou haru sex smart phone 2")
                .replace("ginyou haru this volleyball girl got spiked with a sensual massage", "ginyou haru this volleyball girl got spiked with a sensual massage part 1")
                .replace("kurosu gatari the seika girls and the schoolsanctioned gigolo", "kurosugatari the seika girls and the schoolsanctioned gigolo 1")
                .replace("kurosu gatari the seika girls and the school sanctioned gigolo 6", "kurosu gatari the seika girls and the schoolsanctioned gigolo 6")
                .replace("maimu maimu i met my friends gyaru mom in a soapland 1", "maimumaimu i met my friends gyaru mom in a soapland")
                .replace("sasamori tomoe gaming harem", "sasamori tomoe gaming harem 1")
                .replace("navier haruka 2t succubus delivery vol 30 my report on when i was delivered to a succubus harem that sucked me dry", "navier haruka 2t succubusdelivery vol 30 my report on when i was delivered to a succubus harem that sucked me dry")
                .replace("orico allowance arms race", "orico allowance arms race 1")
                .replace("nishimaki tohru scarlet desire ex 1", "nishimaki tohru scarlet desire ex")
                .replace("navier haruka 2t the sweaty sticky swimmer", "navier haruka 2t the sweaty sticky summer")
                .replace("puuzaki puuna fucked into submission 1", "puuzaki puuna fucked into submission")
                .replace("puuzaki puuna the shy snow woman and the cursed ring chapter 2", "puuzaki puuna the shy snow woman and the cursed ring 2")
                .replace("shake re temptation 1", "shake retemptation 1")
                .replace("shake re temptation 2", "shake retemptation 2")
                .replace("shindou puzzle dragons scrapbook", "shindou pd books puzzle dragons scrapbook")
                .replace("syukuriin all for you", "syukuriin all for you 1")
                .replace("ubuo i lost my tomboy friend to a huge cock", "ubuo i lost my tomboy friend to a huge cock 1")
                .replace("cup chan sex with gender bender kodamachan", "cup chan sex with gender bender kodamachan 1")
                .replace("yagino mekichi i love love love love love love love love you ver 2", "yaginomekichi i love love love love love love love love you ver2")
                .replace("yuuki ringo red light district 2 milfdaughter ntr double the fun", "yuuki ringo red light district milfdaughter ntr double the fun")
                .replace("yuuki ringo red light district 3 milf daughter ntr over the edge", "yuuki ringo red light district milfdaughter ntr over the edge")
                .replace("yuuki ringo red light district milfdaughter ntr 2", "yuuki ringo red light district milfdaughter ntr")
                .replace("konecha world where you can screw anyone", "konecha a world where you can screw anyone")
                .replace("shituzhi i brought home a runaway", "shituzhi i brought home a runaway and she lets me make a mess inside her")
                .replace("opcom arrest thy neighbor 1", "volvox arrest thy neighbor")
                .replace("kuroe little miss debaucherous 7 extra 6", "kuroe little miss debaucherous 7")
                .replace("kuroe little miss debaucherous 3", "kuroe little miss debaucherous 3 wavering heart honeydew in the night")
                .replace("sorono fox widow ms yuiko", "sorono fox widow ms yuiko kanto area in her 30s")
                .replace("navier haruka 2t succubus delivery vol 20", "navier haruka 2t succubus delivery vol 20 my report on the time i was devoured by three succubi")
                .replace("neginegio obscene academy 3 island of despair", "neginegio obscene academy 3")
                .replace("kuroe the shame train 4", "kuroe the shame train 4 satisfying my boyfriends fetish")
                .replace("kuroe little miss debaucherous 2", "kuroe little miss debaucherous 2 public indecency")
                .replace("ind kary choko 5", "ind kary choko 5 requited love unrequited lust")
                .replace("freedom prophet field trip with my first crush", "freedom prophet field trip with my first crush and the bad crowd")
                .replace("benzou stray gyaru harem 2", "benzou stray gyaru harem 2 loveydovey milking session")
                .replace("anzayuu sweet life in another world 5", "anzayuu sweet life in another world 5 are you into an elf mom")
                .replace("shindol metamorphosis", "shindo l metamorphosis emergence")
                .replace("tachibana omina tales of a harem in another world vol 5 vol 55", "tachibana omina tales of a harem in another world vol 5 ambushed the wild succubus sisters appear vol 55")
                .replace("akazawa red i regret to inform that the heros log has disappeared", "girls form - volume 09 - akazawa red i regret to inform that the heros log has disappeared")
                .replace("sorono two flowers for two delivery girls", "sorono two flowers for two delivery girls dog girl and winged girl make steamy happy love with their new dicks")
                .replace("usashiro mani gacen hime to dt otoko no ichaicha kozukuri love sex arcade princess and a virgin boy who make out and have loveydovey babymaking sex", "usashiro mani arcade princess and a virgin boy who make out and have loveydovey babymaking sex")
                .replace("toitoi sachichan no arbeit sachis parttime job", "toitoi sachis parttime job")
                .replace("toitoi sachichan no arbeit 4 sachis parttime job 4", "toitoi sachis parttime job 4")
                .replace("toitoi sachichan no arbeit 3 sachis parttime job 3", "toitoi sachis parttime job 3")
                .replace("toitoi sachichan no arbeit 2 sachis parttime job 2", "toitoi sachis parttime job 2")
                .replace("hanauna yuzuki yukari in dragon quest yuzuki yukaris lewd dragon quest adventure", "hanauna yukari yuzukis lewd dragon quest adventure")
                .replace("hanauna magical toilet girl yuusha 4", "hanauna magical toilet girl yuusha 4 yuushas unlucky losing spree")
                .replace("hanauna magical toilet girl yuusha 3", "hanauna magical toilet girl yuusha 3 yuunas sweet summer vacation")
                .replace("hanauna magical toilet girl yuusha 2", "hanauna magical toilet girl yuusha 2 a tenacious tentacles takedown")
                .replace("satou saori aigan robot lilly pet robot lilly vol 3", "saori sato pet robot lilly volume 3")
                .replace("mutya makochans developoment diary 2", "mutya makochans development diary 2")
                .replace("tsumetoro sexy snakey", "tsumetoro sexy snakey naughty foxy 8")
                .replace("petenshi yukarisan to opuro de nurunurunu chonu cho sugosu hon flirtatious soap play in a bath with yukari", "petenshi flirtatious soap play in a bath with yukari")
                .replace("sanom uraha uchi no dame ane ni osowarete tajitaji nan desu ga my nogood sisters overwhelming seduction technique", "sanom my nogood sisters overwhelming seduction technique")
                .replace("prime secret recipe 3shiname secret recipe vol 3", "prime secret recipe vol 3")
                .replace("kouki kuu kyonyuu no oneechan wa suki desu ka duo do you like big sis big tits duo", "kouki kuu do you like big sis big tits duo")
                .replace("chiyami kono subarashii chaldea ni ai o", "chiyami kamadevas blessing on this wonderful chaldea")
                .replace("rubisama watashi no omocha ni narinasai become my manservant", "rubisama become my manservant")
                .replace("73gou koudou ts hensei shoukougun ts transformation syndrome", "koyu transformation syndrome")
                .replace("indo curry choko iii", "ind kary choko 3 more than a friend less than a girlfriend")
                .replace("indo curry choko iv", "ind kary choko 4 she was her uncles sex toy and then she seduced her cousin")
                .replace("kuroe little miss debaucherous 4", "kuroe little miss debaucherous 4 sensuous moans from his bedside")
                .replace("maimu maimu breaking in the new hire", "maimumaimu breaking in the new hire another mans wife vs the college student")
                .replace("norigorou imaizumin 5", "norigorou imaizumi brings all the gyarus to his house 5")
                .replace("pochi ane naru mono 4", "pochi ane naru mono the eldersister like one 4")
                .replace("pochi ane naru mono 5", "pochi ane naru mono the eldersister like one 5")
                .replace("pochi ane naru mono 6", "pochi ane naru mono the eldersister like one 6")
                .replace("pochi ane naru mono 7", "pochi ane naru mono the eldersister like one 7")
                .replace("syomu my exlovers kid is my sons friend", "syomu sins of the past")
                .replace("booch do lewd things with sapphire 1", "booch do you wanna do lewd things with sapphire 1");
    }
}
