package pro.datawiki.igaming.bots.service;

import org.springframework.stereotype.Service;
import pro.datawiki.igaming.bots.model.BotPair;
import pro.datawiki.igaming.bots.model.BotProfile;

import java.util.ArrayList;
import java.util.List;

@Service
public class BotRosterService {

    private final List<BotPair> pairs = new ArrayList<>();
    private final List<BotProfile> allBots = new ArrayList<>();

    public BotRosterService() {
        initializeRoster();
    }

    private void initializeRoster() {
        // ==========================================
        // 1. USA + USA (Cross-State Arbitrage, 8 pairs)
        // ==========================================
        registerPair("USA_ATX_DEN_1",
                new BotProfile(10001L, "John_ATX", "John Miller", "🤠", "🇺🇸 США (Техас)", "Остин", "USD", List.of("draftkings", "fanduel", "betonline")),
                new BotProfile(10002L, "Sarah_DEN", "Sarah Jenkins", "🏔️", "🇺🇸 США (Колорадо)", "Денвер", "USD", List.of("betmgm", "caesars", "pointsbet"))
        );
        registerPair("USA_NYC_LAS_2",
                new BotProfile(10003L, "Michael_NYC", "Michael Vance", "🗽", "🇺🇸 США (Нью-Йорк)", "Нью-Йорк", "USD", List.of("fanduel", "draftkings", "betrivers")),
                new BotProfile(10004L, "Brian_Vegas", "Brian Cole", "🎰", "🇺🇸 США (Невада)", "Лас-Вегас", "USD", List.of("caesars", "circasports", "superbook"))
        );
        registerPair("USA_MIA_CHI_3",
                new BotProfile(10005L, "Carlos_MIA", "Carlos Diaz", "🌴", "🇺🇸 США (Флорида)", "Майами", "USD", List.of("hardrockbet", "betonline", "bovada")),
                new BotProfile(10006L, "David_CHI", "David Murphy", "🐂", "🇺🇸 США (Иллинойс)", "Чикаго", "USD", List.of("draftkings", "pointsbet", "betrivers"))
        );
        registerPair("USA_LAX_PHL_4",
                new BotProfile(10007L, "Kevin_LA", "Kevin Chen", "🎬", "🇺🇸 США (Калифорния)", "Лос-Анджелес", "USD", List.of("bovada", "betonline", "heritage")),
                new BotProfile(10008L, "Robert_PHL", "Robert Taylor", "🔔", "🇺🇸 США (Пенсильвания)", "Филадельфия", "USD", List.of("betmgm", "fanduel", "caesars"))
        );
        registerPair("USA_SEA_PHX_5",
                new BotProfile(10009L, "Eric_SEA", "Eric Larson", "🌲", "🇺🇸 США (Вашингтон)", "Сиэтл", "USD", List.of("draftkings", "betonline", "bovada")),
                new BotProfile(10010L, "James_PHX", "James Wilson", "🌵", "🇺🇸 США (Аризона)", "Финикс", "USD", List.of("caesars", "fanduel", "betmgm"))
        );
        registerPair("USA_BOS_ATL_6",
                new BotProfile(10011L, "Daniel_BOS", "Daniel O'Connor", "☘️", "🇺🇸 США (Массачусетс)", "Бостон", "USD", List.of("draftkings", "wynnbet", "fanduel")),
                new BotProfile(10012L, "Marcus_ATL", "Marcus Evans", "🍑", "🇺🇸 США (Джорджия)", "Атланта", "USD", List.of("bovada", "betmgm", "betonline"))
        );
        registerPair("USA_DFW_DET_7",
                new BotProfile(10013L, "Tyler_DFW", "Tyler Scott", "⭐", "🇺🇸 США (Техас)", "Даллас", "USD", List.of("betonline", "bovada", "bookmaker_eu")),
                new BotProfile(10014L, "Adam_DET", "Adam Kowalski", "🚗", "🇺🇸 США (Мичиган)", "Детройт", "USD", List.of("betrivers", "fanduel", "caesars"))
        );
        registerPair("USA_SFO_BNA_8",
                new BotProfile(10015L, "Alex_SFO", "Alex Wong", "🌉", "🇺🇸 США (Калифорния)", "Сан-Франциско", "USD", List.of("draftkings", "bovada", "pinnacle")),
                new BotProfile(10016L, "Will_BNA", "William Cooper", "🎸", "🇺🇸 США (Теннесси)", "Нэшвилл", "USD", List.of("betmgm", "action247", "fanduel"))
        );

        // ==========================================
        // 2. Brazil + Brazil (Domestic Latam, 6 pairs)
        // ==========================================
        registerPair("BRA_SAO_RIO_9",
                new BotProfile(10017L, "Mateo_SAO", "Mateo Silva", "⚽", "🇧🇷 Бразилия", "Сан-Паулу", "USD", List.of("betano", "sportingbet", "pinnacle")),
                new BotProfile(10018L, "Lucas_RIO", "Lucas Rocha", "🌊", "🇧🇷 Бразилия", "Рио-де-Жанейро", "USD", List.of("blaze", "kto", "betfair"))
        );
        registerPair("BRA_BHZ_CWB_10",
                new BotProfile(10019L, "Gabriel_BH", "Gabriel Santos", "☕", "🇧🇷 Бразилия", "Белу-Оризонти", "USD", List.of("estrelabet", "betano", "sportingbet")),
                new BotProfile(10020L, "Felipe_CWB", "Felipe Oliveira", "🌲", "🇧🇷 Бразилия", "Куритиба", "USD", List.of("betfair", "pinnacle", "kto"))
        );
        registerPair("BRA_POA_SSA_11",
                new BotProfile(10021L, "Rodrigo_POA", "Rodrigo Costa", "🧉", "🇧🇷 Бразилия", "Порту-Алегри", "USD", List.of("bet365", "sportingbet", "betano")),
                new BotProfile(10022L, "Rafael_SSA", "Rafael Barbosa", "🥁", "🇧🇷 Бразилия", "Салвадор", "USD", List.of("pixbet", "kto", "blaze"))
        );
        registerPair("BRA_FOR_REC_12",
                new BotProfile(10023L, "Thiago_FOR", "Thiago Lima", "☀️", "🇧🇷 Бразилия", "Форталеза", "USD", List.of("betano", "parimatch", "blaze")),
                new BotProfile(10024L, "Bruno_REC", "Bruno Carvalho", "🦈", "🇧🇷 Бразилия", "Ресифи", "USD", List.of("blaze", "betfair", "sportingbet"))
        );
        registerPair("BRA_BSB_GYN_13",
                new BotProfile(10025L, "Gustavo_BSB", "Gustavo Ferreira", "🏛️", "🇧🇷 Бразилия", "Бразилиа", "USD", List.of("bet365", "estrelabet", "betano")),
                new BotProfile(10026L, "Eduardo_GYN", "Eduardo Ribeiro", "🌽", "🇧🇷 Бразилия", "Гояния", "USD", List.of("sportingbet", "kto", "pinnacle"))
        );
        registerPair("BRA_FLN_CPQ_14",
                new BotProfile(10027L, "Henrique_FLN", "Henrique Martins", "🏄", "🇧🇷 Бразилия", "Флорианополис", "USD", List.of("betfair", "pinnacle", "betano")),
                new BotProfile(10028L, "Vinicius_CPQ", "Vinicius Souza", "🔬", "🇧🇷 Бразилия", "Кампинас", "USD", List.of("betano", "blaze", "pixbet"))
        );

        // ==========================================
        // 3. Europe + Europe (Intra-European, 10 pairs)
        // ==========================================
        registerPair("EUR_LON_BER_15",
                new BotProfile(10029L, "Oliver_LON", "Oliver Smith", "🇬🇧", "🇬🇧 Великобритания", "Лондон", "USD", List.of("bet365", "betfair", "williamhill")),
                new BotProfile(10030L, "Lukas_BER", "Lukas Weber", "🇩🇪", "🇩🇪 Германия", "Берлин", "USD", List.of("pinnacle", "bwin", "tipico"))
        );
        registerPair("EUR_PAR_MAD_16",
                new BotProfile(10031L, "Antoine_PAR", "Antoine Dupont", "🥐", "🇫🇷 Франция", "Париж", "USD", List.of("unibet", "betclic", "winamax")),
                new BotProfile(10032L, "Javier_MAD", "Javier Gomez", "🥘", "🇪🇸 Испания", "Мадрид", "USD", List.of("codere", "bet365", "sportium"))
        );
        registerPair("EUR_ROM_AMS_17",
                new BotProfile(10033L, "Matteo_ROM", "Matteo Ricci", "🍕", "🇮🇹 Италия", "Рим", "USD", List.of("snai", "eurobet", "sisal")),
                new BotProfile(10034L, "Daan_AMS", "Daan Van Dijk", "🚲", "🇳🇱 Нидерланды", "Амстердам", "USD", List.of("pinnacle", "unibet", "bet365"))
        );
        registerPair("EUR_DUB_STO_18",
                new BotProfile(10035L, "Liam_DUB", "Liam Murphy", "🍀", "🇮🇪 Ирландия", "Дублин", "USD", List.of("paddypower", "boylesports", "betfair")),
                new BotProfile(10036L, "Erik_STO", "Erik Lindqvist", "👑", "🇸🇪 Швеция", "Стокгольм", "USD", List.of("betsson", "unibet", "nordicbet"))
        );
        registerPair("EUR_VIE_WAW_19",
                new BotProfile(10037L, "Florian_VIE", "Florian Huber", "🎻", "🇦🇹 Австрия", "Вена", "USD", List.of("bwin", "interwetten", "pinnacle")),
                new BotProfile(10038L, "Piotr_WAW", "Piotr Wisniewski", "🦅", "🇵🇱 Польша", "Варшава", "USD", List.of("sts", "fortuna", "superbet"))
        );
        registerPair("EUR_BRU_CPH_20",
                new BotProfile(10039L, "Julien_BRU", "Julien Moreau", "🍫", "🇧🇪 Бельгия", "Брюссель", "USD", List.of("unibet", "ladbrokes", "bingoal")),
                new BotProfile(10040L, "Magnus_CPH", "Magnus Hansen", "🇩🇰", "🇩🇰 Дания", "Копенгаген", "USD", List.of("danske_spil", "bet365", "unibet"))
        );
        registerPair("EUR_LIS_OSL_21",
                new BotProfile(10041L, "Joao_LIS", "Joao Pereira", "⛵", "🇵🇹 Португалия", "Лиссабон", "USD", List.of("betclic", "solverde", "placard")),
                new BotProfile(10042L, "Henrik_OSL", "Henrik Berg", "⛷️", "🇳🇴 Норвегия", "Осло", "USD", List.of("norsk_tipping", "pinnacle", "coolbet"))
        );
        registerPair("EUR_ZUR_HEL_22",
                new BotProfile(10043L, "Noah_ZUR", "Noah Schmidt", "🏔️", "🇨🇭 Швейцария", "Цюрих", "USD", List.of("pinnacle", "swisslos", "bah")),
                new BotProfile(10044L, "Eetu_HEL", "Eetu Korhonen", "🏒", "🇫🇮 Финляндия", "Хельсинки", "USD", List.of("veikkaus", "coolbet", "unibet"))
        );
        registerPair("EUR_PRG_BUD_23",
                new BotProfile(10045L, "Tomas_PRG", "Tomas Dvorak", "🏰", "🇨🇿 Чехия", "Прага", "USD", List.of("tipsport", "fortuna", "chance")),
                new BotProfile(10046L, "Bence_BUD", "Bence Nagy", "🌶️", "🇭🇺 Венгрия", "Будапешт", "USD", List.of("tippmix", "unibet", "bet365"))
        );
        registerPair("EUR_ATH_OTP_24",
                new BotProfile(10047L, "Nikos_ATH", "Nikos Papadopoulos", "🏺", "🇬🇷 Греция", "Афины", "USD", List.of("stoiximan", "novibet", "opap")),
                new BotProfile(10048L, "Andrei_OTP", "Andrei Popa", "🧛", "🇷🇴 Румыния", "Бухарест", "USD", List.of("superbet", "betano", "unibet"))
        );

        // ==========================================
        // 4. Transatlantic: USA + Europe / Latam (8 pairs)
        // ==========================================
        registerPair("TRN_NYC_LON_25",
                new BotProfile(10049L, "Ethan_NYC", "Ethan Wright", "🏙️", "🇺🇸 США (Нью-Йорк)", "Нью-Йорк", "USD", List.of("draftkings", "fanduel", "betonline")),
                new BotProfile(10050L, "Harry_LON", "Harry Davies", "🦁", "🇬🇧 Великобритания", "Лондон", "USD", List.of("betfair", "bet365", "skybet"))
        );
        registerPair("TRN_CHI_AMS_26",
                new BotProfile(10051L, "Thomas_CHI", "Thomas Miller", "🌭", "🇺🇸 США (Иллинойс)", "Чикаго", "USD", List.of("betmgm", "draftkings", "bovada")),
                new BotProfile(10052L, "Lars_AMS", "Lars De Boer", "🌷", "🇳🇱 Нидерланды", "Амстердам", "USD", List.of("pinnacle", "unibet", "betfair"))
        );
        registerPair("TRN_MIA_SAO_27",
                new BotProfile(10053L, "Diego_MIA", "Diego Hernandez", "🕶️", "🇺🇸 США (Флорида)", "Майами", "USD", List.of("hardrockbet", "betonline", "draftkings")),
                new BotProfile(10054L, "Caio_SAO", "Caio Mendonca", "🎉", "🇧🇷 Бразилия", "Сан-Паулу", "USD", List.of("betano", "sportingbet", "pinnacle"))
        );
        registerPair("TRN_LAS_LIM_28",
                new BotProfile(10055L, "Jack_Vegas", "Jack Sullivan", "🎲", "🇺🇸 США (Невада)", "Лас-Вегас", "USD", List.of("caesars", "circasports", "betmgm")),
                new BotProfile(10056L, "Andreas_LIM", "Andreas Christou", "☀️", "🇨🇾 Кипр", "Лимасол", "USD", List.of("pinnacle", "stoiximan", "novibet"))
        );
        registerPair("TRN_SFO_DUB_29",
                new BotProfile(10057L, "Nathan_SFO", "Nathan Bell", "💻", "🇺🇸 США (Калифорния)", "Сан-Франциско", "USD", List.of("bovada", "draftkings", "pinnacle")),
                new BotProfile(10058L, "Conor_DUB", "Conor Walsh", "🍻", "🇮🇪 Ирландия", "Дублин", "USD", List.of("paddypower", "betfair", "bet365"))
        );
        registerPair("TRN_BOS_LIS_30",
                new BotProfile(10059L, "Patrick_BOS", "Patrick Hayes", "🦞", "🇺🇸 США (Массачусетс)", "Бостон", "USD", List.of("draftkings", "pointsbet", "fanduel")),
                new BotProfile(10060L, "Tiago_LIS", "Tiago Neves", "🌊", "🇵🇹 Португалия", "Лиссабон", "USD", List.of("betclic", "pinnacle", "solverde"))
        );
        registerPair("TRN_DFW_FRA_31",
                new BotProfile(10061L, "Mason_DFW", "Mason Ross", "🤠", "🇺🇸 США (Техас)", "Даллас", "USD", List.of("betonline", "bovada", "draftkings")),
                new BotProfile(10062L, "Max_FRA", "Maximilian Koch", "🏦", "🇩🇪 Германия", "Франкфурт", "USD", List.of("tipico", "bwin", "bet365"))
        );
        registerPair("TRN_SEA_MAD_32",
                new BotProfile(10063L, "Logan_SEA", "Logan Clark", "☕", "🇺🇸 США (Вашингтон)", "Сиэтл", "USD", List.of("draftkings", "caesars", "bovada")),
                new BotProfile(10064L, "Alejandro_MAD", "Alejandro Navarro", "🐂", "🇪🇸 Испания", "Мадрид", "USD", List.of("codere", "pinnacle", "betfair"))
        );

        // ==========================================
        // 5. Latam / Asia / Oceania (6 pairs)
        // ==========================================
        registerPair("LAO_BUE_MAD_33",
                new BotProfile(10065L, "Lucas_BUE", "Lucas Rossi", "🥩", "🇦🇷 Аргентина", "Буэнос-Айрес", "USD", List.of("betwarrior", "betfair", "pinnacle")),
                new BotProfile(10066L, "Gonzalo_MAD", "Gonzalo Ramos", "🍷", "🇪🇸 Испания", "Мадрид", "USD", List.of("codere", "bet365", "sportium"))
        );
        registerPair("LAO_SCL_ROM_34",
                new BotProfile(10067L, "Santiago_SCL", "Santiago Fernandez", "🍇", "🇨🇱 Чили", "Сантьяго", "USD", List.of("coolbet", "betsson", "pinnacle")),
                new BotProfile(10068L, "Lorenzo_ROM", "Lorenzo Moretti", "🛵", "🇮🇹 Италия", "Рим", "USD", List.of("eurobet", "pinnacle", "snai"))
        );
        registerPair("LAO_BOG_OPO_35",
                new BotProfile(10069L, "Juan_BOG", "Juan Camilo Vargas", "☕", "🇨🇴 Колумбия", "Богота", "USD", List.of("wplay", "betplay", "pinnacle")),
                new BotProfile(10070L, "Martim_OPO", "Martim Sousa", "🍷", "🇵🇹 Португалия", "Порту", "USD", List.of("betclic", "betfair", "solverde"))
        );
        registerPair("LAO_TYO_SIN_36",
                new BotProfile(10071L, "Kenji_TYO", "Kenji Tanaka", "🗼", "🇯🇵 Япония", "Токио", "USD", List.of("sportsbet_io", "pinnacle", "dafabet")),
                new BotProfile(10072L, "Wei_SIN", "Wei Chen", "🦁", "🇸🇬 Сингапур", "Сингапур", "USD", List.of("sbobet", "dafabet", "pinnacle"))
        );
        registerPair("LAO_SYD_LON_37",
                new BotProfile(10073L, "Jack_SYD", "Jack Thompson", "🦘", "🇦🇺 Австралия", "Сидней", "USD", List.of("tab", "sportsbet", "ladbrokes_au")),
                new BotProfile(10074L, "George_LON", "George Walker", "💂", "🇬🇧 Великобритания", "Лондон", "USD", List.of("betfair", "bet365", "paddypower"))
        );
        registerPair("LAO_MVD_OPO_38",
                new BotProfile(10075L, "Diego_MVD", "Diego Suarez", "🌊", "🇺🇾 Уругвай", "Монтевидео", "USD", List.of("pinnacle", "bet365", "1xbet")),
                new BotProfile(10076L, "Andre_OPO", "Andre Fonseca", "🏰", "🇵🇹 Португалия", "Порту", "USD", List.of("solverde", "betfair", "betclic"))
        );

        // ==========================================
        // 6. Russia / CIS + International (12 pairs)
        // ==========================================
        registerPair("CIS_SPB_ASU_39",
                new BotProfile(10077L, "Ivan_SPb", "Иван Смирнов", "👨‍💻", "🇷🇺 Россия", "Санкт-Петербург", "RUB", List.of("winline", "fonbet", "pari")),
                new BotProfile(10078L, "Carlos_ASU", "Carlos Benitez", "🧉", "🇵🇾 Парагвай", "Асунсьон", "USD", List.of("pinnacle", "bet365", "betfair"))
        );
        registerPair("CIS_MSK_SAO_40",
                new BotProfile(10079L, "Elena_Msk", "Елена Васильева", "👩‍💼", "🇷🇺 Россия", "Москва", "RUB", List.of("fonbet", "betcity", "ligastavok")),
                new BotProfile(10080L, "Felipe_SAO", "Felipe Santos", "🌴", "🇧🇷 Бразилия", "Сан-Паулу", "USD", List.of("betano", "pinnacle", "sportingbet"))
        );
        registerPair("CIS_EKB_BEG_41",
                new BotProfile(10081L, "Dmitry_Ekb", "Дмитрий Морозов", "🏔️", "🇷🇺 Россия", "Екатеринбург", "RUB", List.of("winline", "leon", "baltbet")),
                new BotProfile(10082L, "Stefan_BEG", "Stefan Jovanovic", "🏰", "🇷🇸 Сербия", "Белград", "USD", List.of("mozzartbet", "pinnacle", "maxbet"))
        );
        registerPair("CIS_KZN_LIM_42",
                new BotProfile(10083L, "Artem_Kzn", "Артём Гарифуллин", "🏹", "🇷🇺 Россия", "Казань", "RUB", List.of("olimpbet", "fonbet", "tennisi")),
                new BotProfile(10084L, "Nikos_LIM", "Nikos Papadopoulos", "☀️", "🇨🇾 Кипр", "Лимасол", "USD", List.of("pinnacle", "novibet", "stoiximan"))
        );
        registerPair("CIS_NSK_MLA_43",
                new BotProfile(10085L, "Sergey_Nsk", "Сергей Попов", "❄️", "🇷🇺 Россия", "Новосибирск", "RUB", List.of("betboom", "winline", "pari")),
                new BotProfile(10086L, "Marco_MLA", "Marco Vella", "⛵", "🇲🇹 Мальта", "Валлетта", "USD", List.of("unibet", "pinnacle", "leovegas"))
        );
        registerPair("CIS_SAM_ALA_44",
                new BotProfile(10087L, "Olga_Sam", "Ольга Кузнецова", "🚀", "🇷🇺 Россия", "Самара", "RUB", List.of("pari", "fonbet", "zenit")),
                new BotProfile(10088L, "Arman_ALA", "Арман Касымов", "🦅", "🇰🇿 Казахстан", "Алматы", "USD", List.of("1xbet", "parimatch", "pinnacle"))
        );
        registerPair("CIS_RND_EVN_45",
                new BotProfile(10089L, "Maksim_Rnd", "Максим Романов", "🌾", "🇷🇺 Россия", "Ростов-на-Дону", "RUB", List.of("winline", "ligastavok", "olimpbet")),
                new BotProfile(10090L, "Armen_EVN", "Армен Саркисян", "🏔️", "🇦🇲 Армения", "Ереван", "USD", List.of("vivarobet", "totogaming", "pinnacle"))
        );
        registerPair("CIS_VLA_TYO_46",
                new BotProfile(10091L, "Pavel_Vla", "Павел Белов", "⚓", "🇷🇺 Россия", "Владивосток", "RUB", List.of("fonbet", "betcity", "leon")),
                new BotProfile(10092L, "Daiki_TYO", "Daiki Sato", "🍣", "🇯🇵 Япония", "Токио", "USD", List.of("sbobet", "sportsbet_io", "pinnacle"))
        );
        registerPair("CIS_KRR_TBS_47",
                new BotProfile(10093L, "Anna_Krr", "Анна Соколова", "🌻", "🇷🇺 Россия", "Краснодар", "RUB", List.of("winline", "pari", "baltbet")),
                new BotProfile(10094L, "Giorgi_TBS", "Георгий Беридзе", "🍷", "🇬🇪 Грузия", "Тбилиси", "USD", List.of("crocobet", "adjarabet", "crystalbet"))
        );
        registerPair("CIS_UFA_TAS_48",
                new BotProfile(10095L, "Roman_Ufa", "Роман Бакиев", "🍯", "🇷🇺 Россия", "Уфа", "RUB", List.of("fonbet", "winline", "tennisi")),
                new BotProfile(10096L, "Timur_TAS", "Тимур Алиев", "🕌", "🇺🇿 Узбекистан", "Ташкент", "USD", List.of("1xbet", "mostbet", "pinnacle"))
        );
        registerPair("CIS_GOJ_DXB_49",
                new BotProfile(10097L, "Ilya_NN", "Илья Зайцев", "🏭", "🇷🇺 Россия", "Нижний Новгород", "RUB", List.of("winline", "pari", "fonbet")),
                new BotProfile(10098L, "Zack_DXB", "Zack Al-Mansoor", "🏙️", "🇦🇪 ОАЭ", "Дубай", "USD", List.of("pinnacle", "dafabet", "betfair"))
        );
        registerPair("CIS_MSQ_GDN_50",
                new BotProfile(10099L, "Vlad_MSQ", "Владислав Козлов", "🌲", "🇧🇾 Беларусь", "Минск", "RUB", List.of("marathon", "fonbet", "betcity")),
                new BotProfile(10100L, "Jakub_GDN", "Jakub Zielinski", "⚓", "🇵🇱 Польша", "Гданьск", "USD", List.of("sts", "fortuna", "pinnacle"))
        );
    }

    private void registerPair(String pairName, BotProfile b1, BotProfile b2) {
        BotPair pair = new BotPair(pairName, b1, b2);
        pairs.add(pair);
        allBots.add(b1);
        allBots.add(b2);
    }

    public List<BotPair> getPairs() {
        return pairs;
    }

    public List<BotProfile> getAllBots() {
        return allBots;
    }
}
