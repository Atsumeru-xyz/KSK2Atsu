package xyz.atsumeru.ksk2atsu.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import xyz.atsumeru.ksk2atsu.utils.ArrayUtils;
import xyz.atsumeru.ksk2atsu.utils.Pair;
import xyz.atsumeru.ksk2atsu.utils.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
public class FileMetadata {
    private static final String MAGAZINE_WITH_ISSUE_PATTERN = ".* (\\(([^)]+)\\))\\.cbz";
    private static final String PUBLISHER_PATTERN = "^..*(\\[([^)]+)]).*\\.cbz";

    private static final String SUCH_THING_FIRST_ISSUE_URL_PART = "therebgbs-no-such-thing-as-18-in-this-parallel-world-1-english";

    /**
     * {@link List} of incorrect detected Publishers
     */
    private static final List<String> INCORRECT_PUBLISHERS = List.of(
            "1r0n",
            "all to myself",
            "Dark Souls",
            "Dark Souls",
            "Futa ver",
            "x3100",
            "x3200",
            "C90",
            "Futa ver.",
            "Irodori Comics"
    );

    /**
     * {@link List} of Publisher fixes for some titles without Publisher in file name
     */
    private static final List<Pair<String, String>> TITLES_WITHOUT_PUBLISHER = List.<Pair<String, String>>of(
            // 2D Market
            new Pair<>("2D Market", "Fucking with Portals"),
            new Pair<>("2D Market", "Dream Corridor"),
            new Pair<>("2D Market", "AneOne"),
            new Pair<>("2D Market", "Fuuka-chan's Summer Diary"),
            new Pair<>("2D Market", "The Pink Room and the Nighttime"),
            new Pair<>("2D Market", "KonHaru Sandwich"),
            new Pair<>("2D Market", "My Lovely Commander"),
            new Pair<>("2D Market", "Sweet Sweet Kashima"),
            new Pair<>("2D Market", "VALKYRIA"),
            new Pair<>("2D Market", "Gentleman's Maid Sophie"),
            new Pair<>("2D Market", "I Love My Admiral"),
            new Pair<>("2D Market", "Magical Toilet Girl"),
            new Pair<>("2D Market", "How Did I Get"),
            new Pair<>("2D Market", "My Elder Sister"),
            new Pair<>("2D Market", "Fapdroid Sex Life"),
            new Pair<>("2D Market", "Have Gone To The Doujinshi"),
            new Pair<>("2D Market", "Development Diary"),
            new Pair<>("2D Market", "Ei Freeway"),
            new Pair<>("2D Market", "Sexcellent"),
            new Pair<>("2D Market", "Night Hypnotism Patisserie"),
            new Pair<>("2D Market", "Evil At Heart"),
            new Pair<>("2D Market", "Princess Who Became"),
            new Pair<>("2D Market", "This Time Her Daughter"),
            new Pair<>("2D Market", "Do You Like Naughty Older"),
            new Pair<>("2D Market", "Mash, The Lewd Servant"),
            new Pair<>("2D Market", "What Happens When You Gender Bend"),
            new Pair<>("2D Market", "Bedtime with Mom"),
            new Pair<>("2D Market", "Half of PRISM"),
            new Pair<>("2D Market", "Koh LOVE-Ru"),
            new Pair<>("2D Market", "Can You Hear the Sound"),
            new Pair<>("2D Market", "Midnight Cranes"),
            new Pair<>("2D Market", "Morning Milk"),
            new Pair<>("2D Market", "My Kouhai Maid"),
            new Pair<>("2D Market", "Before Becoming Yours"),
            new Pair<>("2D Market", "Rabbitch Fuck"),
            // Denpasoft
            new Pair<>("Denpasoft", "My Ideal Life in Another World"),
            new Pair<>("Denpasoft", "Lesbian Collection"),
            new Pair<>("Denpasoft", "A Book Where Miku"),
            new Pair<>("Denpasoft", "The Fallen Charismatic Cosplayer"),
            new Pair<>("Denpasoft", "This is Really"),
            new Pair<>("Denpasoft", "Do You Like Hentai Doujinshi"),
            new Pair<>("Denpasoft", "Become My Manservan"),
            // FAKKU
            new Pair<>("FAKKU", "Dick Note The Hypnotic Sexual Guidance"),
            new Pair<>("FAKKU", "My Meat Brings All the Gyarus to the Yard"),
            new Pair<>("FAKKU", "We're No-Nonsense Goody Two Shoes"),
            new Pair<>("FAKKU", "Spending a Sweaty, Lazy Summer"),
            new Pair<>("FAKKU", "Assisted Mating"),
            new Pair<>("FAKKU", "Meaty Minxes"),
            new Pair<>("FAKKU", "A Body for Play"),
            new Pair<>("FAKKU", "Peachy-Butt Girls"),
            new Pair<>("FAKKU", "Maki-chan's First Time With Nico-chan"),
            new Pair<>("FAKKU", "Alluring Woman"),
            new Pair<>("FAKKU", "The Otaku in 10,000 B.C."),
            new Pair<>("FAKKU", "PANDRA"),
            new Pair<>("FAKKU", "Hot Shit High"),
            new Pair<>("FAKKU", "Curiosity XXXed"),
            new Pair<>("FAKKU", "Took the Nympho-Only Women's"),
            new Pair<>("FAKKU", "Interdimensional Brothel"),
            new Pair<>("FAKKU", "Kogals, Sluts, and Whatever"),
            new Pair<>("FAKKU", "Kira Kira"),
            new Pair<>("FAKKU", "Shoujo Material"),
            new Pair<>("FAKKU", "Misdirection"),
            new Pair<>("FAKKU", "Porno Switch"),
            new Pair<>("FAKKU", "Bashful Break"),
            new Pair<>("FAKKU", "Renai Sample"),
            new Pair<>("FAKKU", "Melty Gaze"),
            new Pair<>("FAKKU", "Fresh Pudding"),
            new Pair<>("FAKKU", "TiTiKEi"),
            new Pair<>("FAKKU", "The Neighbor Next Door"),
            new Pair<>("FAKKU", "Another's Wife"),
            new Pair<>("FAKKU", "After School Vanilla"),
            new Pair<>("FAKKU", "Straight Line Once"),
            new Pair<>("FAKKU", "Welcome to Tokoharu"),
            new Pair<>("FAKKU", "The Job of a Service"),
            new Pair<>("FAKKU", "40 Year Old Grand"),
            new Pair<>("FAKKU", "Hurl Ryuu"),
            new Pair<>("FAKKU", "Let's Get Horny"),
            new Pair<>("FAKKU", "Our Pet Danua"),
            new Pair<>("FAKKU", "XXX Maiden"),
            new Pair<>("FAKKU", "Carnal Communication"),
            new Pair<>("FAKKU", "Mogudan Illust"),
            new Pair<>("FAKKU", "Loose With Lewd"),
            new Pair<>("FAKKU", "the Pink"),
            new Pair<>("FAKKU", "Love-Ridden"),
            new Pair<>("FAKKU", "Pandemonium"),
            new Pair<>("FAKKU", "Frisky Fever"),
            new Pair<>("FAKKU", "Hanafuda"),
            new Pair<>("FAKKU", "PuniKano"),
            new Pair<>("FAKKU", "Lets Do It"),
            new Pair<>("FAKKU", "Saitom Box"),
            new Pair<>("FAKKU", "How I Went From"),
            new Pair<>("FAKKU", "When Budding Lilies"),
            new Pair<>("FAKKU", "Ima Real"),
            new Pair<>("FAKKU", "Little Sister Downgrade"),
            new Pair<>("FAKKU", "Transgressed Slave Oni"),
            new Pair<>("FAKKU", "An Saya Banglissimo"),
            new Pair<>("FAKKU", "After Hours"),
            // Irodori Comics
            new Pair<>("Irodori Comics", "Sweet Life in Another World Are You Into An Older Elf"),
            new Pair<>("Irodori Comics", "Became a Mage in Another World"),
            new Pair<>("Irodori Comics", "Summer With Misono"),
            new Pair<>("Irodori Comics", "Summer with Misono"),
            new Pair<>("Irodori Comics", "The Student Council's Demand"),
            new Pair<>("Irodori Comics", "Hypnotic Sexual Counseling"),
            new Pair<>("Irodori Comics", "Rika's Sex Den"),
            new Pair<>("Irodori Comics", "The Hostess of This Esteemed Hot Springs"),
            new Pair<>("Irodori Comics", "Covered in Milk"),
            new Pair<>("Irodori Comics", "Extracurricular Lessons"),
            new Pair<>("Irodori Comics", "Training with Sis"),
            new Pair<>("Irodori Comics", "Futa Wives' Fuck Toy"),
            new Pair<>("Irodori Comics", "Welcome to Mizuryukei Land"),
            new Pair<>("Irodori Comics", "Hot Springs With My Older Girlfriend"),
            new Pair<>("Irodori Comics", "Meaty Meaty Ikumi"),
            new Pair<>("Irodori Comics", "Shinobu's Divorcee Body"),
            new Pair<>("Irodori Comics", "Swimming with Sayaka"),
            new Pair<>("Irodori Comics", "Sakurai"),
            new Pair<>("Irodori Comics", "Summer Sacrifice"),
            new Pair<>("Irodori Comics", "Drooly Sex"),
            new Pair<>("Irodori Comics", "Lovey-Dovey, Lickie, Sticky Sex with Baby-Faced Teacher"),
            new Pair<>("Irodori Comics", "I'm Not Your Idol"),
            new Pair<>("Irodori Comics", "I Sexually Trained a Delinquent Girl"),
            new Pair<>("Irodori Comics", "Let Big Sis Help You Let it All Out"),
            new Pair<>("Irodori Comics", "Chaldea Sex Rotation"),
            new Pair<>("Irodori Comics", "Kaoruko's Fervent Study"),
            new Pair<>("Irodori Comics", "Please XX With Me, Silva"),
            new Pair<>("Irodori Comics", "Mating with Oni"),
            new Pair<>("Irodori Comics", "No One Does It Like You"),
            new Pair<>("Irodori Comics", "No one Does It Like You"),
            new Pair<>("Irodori Comics", "Silent Manga Omnibus"),
            new Pair<>("Irodori Comics", "Curvy Tales"),
            new Pair<>("Irodori Comics", "Choko"),
            new Pair<>("Irodori Comics", "Cucked With My Consent The"),
            new Pair<>("Irodori Comics", "Living with Succubus"),
            new Pair<>("Irodori Comics", "I Love You So"),
            new Pair<>("Irodori Comics", "Prostitution - School Girl"),
            new Pair<>("Irodori Comics", "Tropical Island Maniacs"),
            new Pair<>("Irodori Comics", "Friends with Benefits"),
            new Pair<>("Irodori Comics", "Big Sis Loves Nobody Else"),
            new Pair<>("Irodori Comics", "Dr. Hazuki's Mating"),
            new Pair<>("Irodori Comics", "Can a Little Orc"),
            new Pair<>("Irodori Comics", "Sex Manual by Blood Type"),
            new Pair<>("Irodori Comics", "Post-Exam Treats"),
            new Pair<>("Irodori Comics", "Post-Test Treats"),
            new Pair<>("Irodori Comics", "Memoir of a Cheating Missus"),
            new Pair<>("Irodori Comics", "Compensated Dating"),
            new Pair<>("Irodori Comics", "Pound Town with the New"),
            new Pair<>("Irodori Comics", "No one Does it Like You Brother"),
            new Pair<>("Irodori Comics", "Electric Footsie"),
            new Pair<>("Irodori Comics", "Runaway Elf"),
            new Pair<>("Irodori Comics", "Lessons All Day"),
            new Pair<>("Irodori Comics", "Our Summer Secret"),
            new Pair<>("Irodori Comics", "This Volleyball Girl"),
            new Pair<>("Irodori Comics", "Shame Train"),
            new Pair<>("Irodori Comics", "Breaking In The New Hire"),
            new Pair<>("Irodori Comics", "Would You Still Love Me"),
            new Pair<>("Irodori Comics", "Succubus Delivery"),
            new Pair<>("Irodori Comics", "The Oni and the Fresh Peach"),
            new Pair<>("Irodori Comics", "The Tanuki's Lover"),
            new Pair<>("Irodori Comics", "The Siren's Cradle"),
            new Pair<>("Irodori Comics", "Enticed by the Oasis"),
            new Pair<>("Irodori Comics", "Succubus Landlady"),
            new Pair<>("Irodori Comics", "Ane naru mono"),
            new Pair<>("Irodori Comics", "Ane Naru Mono"),
            new Pair<>("Irodori Comics", "Notice Me, Ayu"),
            new Pair<>("Irodori Comics", "A Married Woman"),
            new Pair<>("Irodori Comics", "Mother Knows Best"),
            new Pair<>("Irodori Comics", "Shrine Maiden's Lost Purity"),
            new Pair<>("Irodori Comics", "Puzzle Dragons Scrapbook"),
            new Pair<>("Irodori Comics", "There's Just No Beating"),
            new Pair<>("Irodori Comics", "Young Cock Party"),
            new Pair<>("Irodori Comics", "28 Year Old Widow"),
            new Pair<>("Irodori Comics", "Feathery Maid Miss Yachiyo"),
            new Pair<>("Irodori Comics", "Life With a Succubus"),
            new Pair<>("Irodori Comics", "The Elf Shopkeeper"),
            new Pair<>("Irodori Comics", "What a Sassy Sex Doll"),
            new Pair<>("Irodori Comics", "The Everlasting Elf of the Evening"),
            new Pair<>("Irodori Comics", "My Best Friend is a Gender Bender"),
            new Pair<>("Irodori Comics", "Sex With Gender Bender"),
            new Pair<>("Irodori Comics", "Zebra's Narcolepsy"),
            new Pair<>("Irodori Comics", "Awashima Harem"),
            new Pair<>("Irodori Comics", "Sex Makes a Hard Worker"),
            new Pair<>("Irodori Comics", "Recharge with Newbie Succubus"),
            new Pair<>("Irodori Comics", "Give for You"),
            new Pair<>("Irodori Comics", "Asleep at the Training Camp"),
            new Pair<>("Irodori Comics", "Hot Springs with Ms. Marie"),
            new Pair<>("Irodori Comics", "Hot Springs with Yuki"),
            new Pair<>("Irodori Comics", "Red Light District"),
            new Pair<>("Irodori Comics", "Otaku vs. Succubus"),
            // MediBang
            new Pair<>("MediBang", "Corrections Officer Rebellious"),
            new Pair<>("MediBang", "The Virgin Girl Who Wet Herself"),
            // Yuri-ism
            new Pair<>("Yuri-ism", "Office Sweet 365"),
            new Pair<>("Yuri-ism", "Rule of Zero"),
            // Summer Salt
            new Pair<>("Summer Salt", "The Duties of the Bloodhound Maid"),
            new Pair<>("Summer Salt", "Factory of Nekoi"),
            new Pair<>("Summer Salt", "Nighttime Nero"),
            // ENSHODO
            new Pair<>("ENSHODO", "Do Lewd Things With Sapphire"),
            new Pair<>("ENSHODO", "Heart for Boyfriend"),
            // CyberuniqueART
            new Pair<>("CyberuniqueART", "Save Me, My Goddess"),
            // Da Hootch
            new Pair<>("Da Hootch", "Super Panpanronpa"),
            new Pair<>("Da Hootch", "Mildred"),
            new Pair<>("Da Hootch", "Super Lychee Juice"),
            // Kagura Games
            new Pair<>("Kagura Games", "TS Bad Ending"),
            // FuDeORS
            new Pair<>("FuDeORS", "The Ogre Girl and The Traveler"),
            // Screamo
            new Pair<>("Screamo", "Cowgirl's Riding-Position"),
            // PUSH!
            new Pair<>("PUSH", "Hot Spring Circle"),
            new Pair<>("PUSH", "Tonight, These"),
            new Pair<>("PUSH", "White Secret"),
            // Lana Rain
            new Pair<>("Lana Rain", "[Eudetenis] Train")
    );

    @Getter
    private File file;
    private YAMLContent yamlContent;
    private BookInfo bookInfo;

    /**
     * Get Volume with leading zeroes formatted number
     *
     * @param volumeNumber input {@link String} volume number
     * @return formatted Volume {@link String}
     */
    private static String getVolumeNumberWithLeadingZeroes(String volumeNumber) {
        if (volumeNumber.contains("Volume")) {
            String realVolume = volumeNumber.replaceAll(".*Volume (\\d+)", "$1");
            return volumeNumber.replace(realVolume, String.format("%02d", Integer.valueOf(realVolume)));
        }
        return volumeNumber;
    }

    /**
     * Get Url for file from {@link YAMLContent#getURL()} or {@link BookInfo#getLink()}
     *
     * @return {@link String} url
     */
    public String getUrl() {
        return Optional.ofNullable(yamlContent)
                .map(YAMLContent::getURL)
                .orElseGet(() -> Optional.ofNullable(bookInfo)
                        .map(BookInfo::getLink)
                        .orElse(null));
    }

    /**
     * Get Magazine for file from {@link YAMLContent#getMagazine()}  or {@link BookInfo#getMagazines()} ()}
     *
     * @return {@link String} magazine
     */
    public String getMagazine() {
        String magazine = Optional.ofNullable(yamlContent)
                .map(YAMLContent::getMagazine)
                .filter(ArrayUtils::isNotEmpty)
                .map(magazines -> magazines.get(getMagazineIndex()))
                .orElseGet(
                        () -> Optional.ofNullable(bookInfo)
                                .map(BookInfo::getMagazines)
                                .filter(ArrayUtils::isNotEmpty)
                                .map(magazines -> magazines.get(0))
                                .orElseGet(this::getMagazineWithIssueFromFileName)
                );

        return isValidMagazine(magazine) ? magazine : null;
    }

    /**
     * Get Magazine index in {@link YAMLContent#getMagazine()} {@link List}. Only in one case this index will be non-zero:
     * if {@link YAMLContent#getURL()} contains {@link #SUCH_THING_FIRST_ISSUE_URL_PART}
     *
     * @return Magazine index in {@link YAMLContent#getMagazine()} {@link List}
     */
    private int getMagazineIndex() {
        return yamlContent.getURL().toLowerCase().contains(SUCH_THING_FIRST_ISSUE_URL_PART) ? 1 : 0;
    }

    /**
     * Checks is provided {@link String} is valid Magazine
     *
     * @param magazine input {@link String} to check
     * @return thue if valid Magazine
     */
    private boolean isValidMagazine(String magazine) {
        return INCORRECT_PUBLISHERS.stream().noneMatch(publisher -> StringUtils.equalsIgnoreCase(magazine, publisher));
    }

    /**
     * Get Publisher for file from {@link BookInfo#getPublisher()} or from file name using {@link #PUBLISHER_PATTERN}
     * with fixes for some known problems and broken titles
     *
     * @return {@link String} publisher
     */
    public String getPublisher() {
        return Optional.ofNullable(bookInfo)
                .map(BookInfo::getPublisher)
                .orElseGet(() -> {
                    String originalName = file.getName().replace("(1)", "");
                    String name = originalName.replaceAll(PUBLISHER_PATTERN, "$2");
                    return !StringUtils.equalsIgnoreCase(originalName, name) ? fixSomeKnownPublisherProblems(originalName, name) : getPublisherForSomeBrokenTitles(originalName);
                });
    }

    /**
     * Get Publisher from file name using fixes from {@link #TITLES_WITHOUT_PUBLISHER} {@link Map}
     *
     * @param fileName input {@link String} file name
     * @return {@link String} publisher
     */
    private String getPublisherForSomeBrokenTitles(String fileName) {
        return TITLES_WITHOUT_PUBLISHER.stream()
                .filter(pair -> fileName.contains(pair.second))
                .findFirst().map(pair -> pair.first)
                .orElse(null);
    }

    /**
     * Fix some known Publisher parsing problems
     *
     * @param fileName  input {@link String} file name
     * @param publisher input {@link String} publisher
     * @return fixed {@link String} publisher
     */
    private String fixSomeKnownPublisherProblems(String fileName, String publisher) {
        if (StringUtils.equalsIgnoreCase(publisher, "Digital") && fileName.toLowerCase().contains("2d-market")) {
            return "2D Market";
        }
        return publisher.replaceAll(".*]\\[", "")
                .trim()
                .replace("FAKKU!", "FAKKU")
                .replaceAll("^Fakku$", "FAKKU")
                .replaceAll("^1r0n$", "FAKKU")
                .replaceAll("^OT$", "FAKKU & Irodori Comics")
                .replaceAll("^oppaitime$", "FAKKU & Irodori Comics")
                .replaceAll("^oppaitime$", "Irodori Comics")
                .replace("Anonymous_Friend", "2D Market")
                .replace("FAKKU 2D Market", "FAKKU & 2D Market")
                .replace("FAKKU Original FAKKU Original", "FAKKU Original")
                .replace("FAKKU Irodori Comics", "FAKKU & Irodori Comics")
                .replaceAll("^FAKKU & Irodori$", "FAKKU & Irodori Comics")
                .replace("FAKKU & MediBang!", "FAKKU & MediBang")
                .replaceAll("^FAKKU & MediBang$", "FAKKU & MediBang")
                .replaceAll("^MediBang!$", "MediBang")
                .replaceAll("PUSH!", "PUSH")
                .replaceAll("^PUSH$", "PUSH")
                .replace("2d-market.com", "2D Market")
                .replace("not FAKKU", "Irodori Comics")
                .replace("Irodori_Comics", "Irodori Comics")
                .replaceAll("^Irodori Comic$", "Irodori Comics")
                .replaceAll("FAKKU & ", "")
                .replaceAll("Fakku & ", "");
    }

    /**
     * Parse Magazine with Issue from file name with {@link #MAGAZINE_WITH_ISSUE_PATTERN}
     *
     * @return {@link String} Magazine with Issue
     */
    private String getMagazineWithIssueFromFileName() {
        String originalName = file.getName().replace("(1)", "");
        String name = originalName.replaceAll(MAGAZINE_WITH_ISSUE_PATTERN, "$2");
        return !StringUtils.equalsIgnoreCase(originalName, name) ? name : null;
    }

    /**
     * Get Magazine name without Issue from {@link String}
     *
     * @param magazine input magazine with issue {@link String}
     * @return magazine without issue {@link String}
     */
    public String getMagazineName(String magazine) {
        return magazine.replaceAll(" \\d+-\\d+", "")
                .replaceAll(" #\\d+", "")
                .replaceAll(" Vol.\\d+", "")
                .replaceAll(" \\d+ \\w+", "")
                .replace("Comic", "COMIC");
    }

    /**
     * Get Issue name without Magazine from {@link String}
     *
     * @param magazine input magazine with issue {@link String}
     * @return issue without magazine {@link String}
     */
    public String getMagazineIssue(String magazine) {
        return getVolumeNumberWithLeadingZeroes(
                magazine.replaceAll(".* (\\d+-\\d+)", "[$1]")
                        .replaceAll(".* (#\\d+)", "$1")
                        .replaceAll(".* Vol.(\\d+)", "- Volume $1")
                        .replaceAll(".* (\\d+ \\w+)", "[$1]")
        );
    }
}
