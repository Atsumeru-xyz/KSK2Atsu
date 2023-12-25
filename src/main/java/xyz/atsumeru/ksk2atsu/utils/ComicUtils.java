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
    public static String getTitleWithArtistAndReplacedDeniedSymbols(String title, String artist) {
        return String.format("[%s] %s", StringUtils.isNotEmpty(artist) ? artist : App.UNKNOWN, getTitleReplacedDeniedSymbols(title));
    }

    /**
     * Replace denied Windows FS symbols for special Japanese analogs in given title with author {@link String} that may
     * be used for file naming with additional name cleaning from square, round, curly brackets and some predefined "trash"
     *
     * @param title input {@link String}
     * @return safe Windows FS filename
     */
    public static String getTitleWithAuthorAndReplacedDeniedSymbols(String title) {
        return getTitleReplacedDeniedSymbols(title.replace(".cbz", ""))
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
                .toLowerCase()
                .trim();
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
        if (title.contains("yukari-san to opuro de nurunurunu")) {
            return "flirtatious soap play in a bath with yukari";
        } else if (title.contains("uchi no dame ane ni osowarete tajitaji")) {
            return "my no-good sister's overwhelming seduction technique";
        } else if (title.contains("secret recipe 3-shiname secret recipe vol. 3")) {
            return "secret recipe vol. 3";
        } else if (title.contains("sexy snakey")) {
            return "sexy snakey (naughty foxy 8)";
        } else if (title.contains("kyonyuu no onee-chan wa suki desu")) {
            return "do you like big sis' big tits? duo";
        } else if (title.contains("kono subarashii chaldea ni ai o")) {
            return "kamadeva's blessing on this wonderful chaldea";
        } else if (title.contains("aigan robot lilly - pet robot lilly vol. 3")) {
            return "pet robot lilly - volume 3";
        } else if (title.contains("yuzuki yukari in dragon quest yuzuki yukari's lewd dragon quest adventure")) {
            return "yukari yuzuki's lewd dragon quest adventure";
        } else if (title.contains("sachi-chan no arbeit 2")) {
            return "sachi's part-time job 2";
        } else if (title.contains("sachi-chan no arbeit 3")) {
            return "sachi's part-time job 3";
        } else if (title.contains("sachi-chan no arbeit 4")) {
            return "sachi's part-time job 4";
        } else if (title.contains("sachi's part-time job")) {
            return "sachi's part-time job";
        } else if (title.contains("love sex arcade princess and a virgin boy")) {
            return "arcade princess and a virgin boy who make out and have lovey-dovey baby-making sex";
        } else if (title.contains("become my manservant")) {
            return "become my manservant";
        } else if (title.contains("transformation syndrome")) {
            return "transformation syndrome";
        } else if (title.contains("do lewd things with sapphire 1")) {
            return "do you wanna do lewd things with sapphire 1";
        } else if (title.contains("two flowers for two delivery")) {
            return "two flowers for two delivery girls - dog girl and winged girl make steamy happy love with their new dicks";
        } else if (title.contains("hypnotic sexual counseling 2.5")) {
            return "hypnotic sexual counseling 2.5";
        } else if (title.contains("mating 11")) {
            return "assisted mating 11";
        } else if (title.contains("obscene academy 3")) {
            return "obscene academy 3";
        } else if (title.contains("breaking in the new hire")) {
            return "breaking in the new hire (another man's wife vs the college student)";
        } else if (title.contains("choko 5")) {
            return "choko 5 - requited love, unrequited lust";
        } else if (title.contains("choko iii")) {
            return "choko 3 - more than a friend, less than a girlfriend";
        } else if (title.contains("choko iv")) {
            return "choko 4 - she was her uncle's sex toy, and then she seduced her cousin";
        } else if (title.contains("the shame train 4")) {
            return "the shame train 4 - satisfying my boyfriend's fetish";
        } else if (title.contains("succubus delivery?? vol. 2.0")) {
            return "succubus delivery  vol. 2.0 - my report on the time i was devoured by three succubi";
        } else if (title.contains("succubus delivery")) {
            return "succubus delivery my report on the time i called a pair of succubus call girls";
        } else if (title.contains("ane naru mono 4")) {
            return "ane naru mono the elder-sister like one 4";
        } else if (title.contains("ane naru mono 5")) {
            return "ane naru mono the elder-sister like one 5";
        } else if (title.contains("ane naru mono 6")) {
            return "ane naru mono the elder-sister like one 6";
        } else if (title.contains("ane naru mono 7")) {
            return "ane naru mono the elder-sister like one 7";
        } else if (title.contains("puzzle dragons scrapbook")) {
            return "p&d books - puzzle & dragons scrapbook";
        } else if (title.contains("fox widow")) {
            return "fox widow ms. yuiko (kanto area, in her 30s)";
        } else if (title.contains("little miss debaucherous 7")) {
            return "little miss debaucherous 7";
        } else if (title.contains("i brought home a runaway")) {
            return "i brought home a runaway (and she lets me make a mess inside her)";
        } else if (title.contains("imaizumin 5")) {
            return "imaizumi brings all the gyarus to his house 5";
        } else if (title.contains("my ex-lovers kid is my sons friend")) {
            return "sins of the past";
        } else if (title.contains("i regret to inform that")) {
            return "girls form - volume 09 - [akazawa red] i regret to inform that the hero's log has disappeared";
        } else if (title.contains("metamorphosis")) {
            return "metamorphosis (emergence)";
        } else if (title.contains("hands-on draining with three succubus sisters - ch. 1")) {
            return "hands-on draining with three succubus sisters - chapter 1: lamy's secret";
        } else if (title.contains("sweet life in another world 5")) {
            return "sweet life in another world 5: are you into an elf mom";
        } else if (title.contains("the biggest loser")) {
            return "the biggest loser (winner)";
        } else if (title.contains("field trip with my first crush")) {
            return "field trip with my first crush and the bad crowd";
        } else if (title.contains("no one does it like you brother")) {
            return "sasuoni! - no one does it like you, brother! 1";
        }
        return title;
    }
}
