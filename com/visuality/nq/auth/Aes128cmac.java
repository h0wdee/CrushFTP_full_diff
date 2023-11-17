/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.auth.CmacCtx;
import com.visuality.nq.common.Blob;
import java.util.Arrays;

public class Aes128cmac {
    public static final int AES_128_SECURITY_SIGNATURE_SIZE = 16;
    private static final int[] AES_SBox = new int[]{0x63636363, 0x7C7C7C7C, 0x77777777, 0x7B7B7B7B, -218959118, 0x6B6B6B6B, 0x6F6F6F6F, -976894523, 0x30303030, 0x1010101, 0x67676767, 0x2B2B2B2B, -16843010, -673720361, -1414812757, 0x76767676, -892679478, -2105376126, -909522487, 0x7D7D7D7D, -84215046, 0x59595959, 0x47474747, -252645136, -1381126739, -724249388, -1566399838, -1347440721, -1667457892, -1532713820, 0x72727272, -1061109568, -1212696649, -33686019, -1819044973, 0x26262626, 0x36363636, 0x3F3F3F3F, -134744073, -858993460, 0x34343434, -1515870811, -437918235, -235802127, 0x71717171, -656877352, 0x31313131, 0x15151515, 0x4040404, -943208505, 0x23232323, -1010580541, 0x18181818, -1768515946, 0x5050505, -1701143910, 0x7070707, 0x12121212, -2139062144, -488447262, -336860181, 0x27272727, -1296911694, 0x75757575, 0x9090909, -2088533117, 0x2C2C2C2C, 0x1A1A1A1A, 0x1B1B1B1B, 0x6E6E6E6E, 0x5A5A5A5A, -1600085856, 0x52525252, 0x3B3B3B3B, -690563370, -1280068685, 0x29292929, -471604253, 0x2F2F2F2F, -2071690108, 0x53535353, -774778415, 0, -303174163, 0x20202020, -50529028, -1313754703, 0x5B5B5B5B, 0x6A6A6A6A, -875836469, -1094795586, 0x39393939, 0x4A4A4A4A, 0x4C4C4C4C, 0x58585858, -808464433, -791621424, -269488145, -1431655766, -67372037, 0x43434343, 0x4D4D4D4D, 0x33333333, -2054847099, 0x45454545, -101058055, 0x2020202, 0x7F7F7F7F, 0x50505050, 0x3C3C3C3C, -1616928865, -1465341784, 0x51515151, -1549556829, 0x40404040, -1886417009, -1835887982, -1650614883, 0x38383838, -168430091, -1128481604, -1229539658, -623191334, 0x21212121, 0x10101010, -1, -202116109, -757935406, -842150451, 0xC0C0C0C, 0x13131313, -320017172, 0x5F5F5F5F, -1751672937, 0x44444444, 0x17171717, -993737532, -1482184793, 0x7E7E7E7E, 0x3D3D3D3D, 0x64646464, 0x5D5D5D5D, 0x19191919, 0x73737373, 0x60606060, -2122219135, 0x4F4F4F4F, -589505316, 0x22222222, 0x2A2A2A2A, -1869574000, -2004318072, 0x46464646, -286331154, -1195853640, 0x14141414, -555819298, 0x5E5E5E5E, 0xB0B0B0B, -606348325, -522133280, 0x32323232, 0x3A3A3A3A, 0xA0A0A0A, 0x49494949, 0x6060606, 0x24242424, 0x5C5C5C5C, -1027423550, -741092397, -1397969748, 0x62626262, -1852730991, -1785358955, -454761244, 0x79797979, -404232217, -926365496, 0x37373737, 0x6D6D6D6D, -1920103027, -707406379, 0x4E4E4E4E, -1448498775, 0x6C6C6C6C, 0x56565656, -185273100, -353703190, 0x65656565, 0x7A7A7A7A, -1364283730, 0x8080808, -1162167622, 0x78787878, 0x25252525, 0x2E2E2E2E, 0x1C1C1C1C, -1499027802, -1263225676, -960051514, -387389208, -572662307, 0x74747474, 0x1F1F1F1F, 0x4B4B4B4B, -1111638595, -1953789045, -1970632054, 0x70707070, 0x3E3E3E3E, -1246382667, 0x66666666, 0x48484848, 0x3030303, -151587082, 0xE0E0E0E, 0x61616161, 0x35353535, 0x57575757, -1179010631, -2038004090, -1044266559, 0x1D1D1D1D, -1633771874, -505290271, -117901064, -1734829928, 0x11111111, 0x69696969, -640034343, -1903260018, -1802201964, -1684300901, 0x1E1E1E1E, -2021161081, -370546199, -825307442, 0x55555555, 0x28282828, -538976289, -1936946036, -1583242847, -1987475063, 0xD0D0D0D, -1077952577, -421075226, 0x42424242, 0x68686868, 0x41414141, -1717986919, 0x2D2D2D2D, 0xF0F0F0F, -1330597712, 0x54545454, -1145324613, 0x16161616};
    private static final int[] AES_128_Rcon = new int[]{0x1000000, 0x2000000, 0x4000000, 0x8000000, 0x10000000, 0x20000000, 0x40000000, Integer.MIN_VALUE, 0x1B000000, 0x36000000};
    private static final int[] AES_Table_1 = new int[]{-966564955, -126059388, -294160487, -159679603, -855539, -697603139, -563122255, -1849309868, 1613770832, 33620227, -832084055, 1445669757, -402719207, -1244145822, 1303096294, -327780710, -1882535355, 528646813, -1983264448, -92439161, -268764651, -1302767125, -1907931191, -68095989, 1101901292, -1277897625, 1604494077, 1169141738, 597466303, 1403299063, -462261610, -1681866661, 1974974402, -503448292, 1033081774, 1277568618, 1815492186, 2118074177, -168298750, -2083730353, 1748251740, 1369810420, -773462732, -101584632, -495881837, -1411852173, 1647391059, 706024767, 134480908, -1782069422, 1176707941, -1648114850, 806885416, 932615841, 168101135, 798661301, 235341577, 605164086, 461406363, -538779075, -840176858, 1311188841, 2142417613, -361400929, 302582043, 495158174, 1479289972, 874125870, 907746093, -596742478, -1269146898, 1537253627, -1538108682, 1983593293, -1210657183, 2108928974, 1378429307, -572267714, 1580150641, 327451799, -1504488459, -1177431704, 0, -1041371860, 1075847264, -469959649, 2041688520, -1235526675, -731223362, -1916023994, 1740553945, 1916352843, -1807070498, -1739830060, -1336387352, -2049978550, -1143943061, -974131414, 1336584933, -302253290, -2042412091, -1706209833, 0x66333355, 293963156, -1975171633, -369493744, 67240454, -25198719, -1605349136, 2017213508, 631218106, 1269344483, -1571728909, 1571005438, -2143272768, 93294474, 1066570413, 563977660, 1882732616, -235539196, 1673313503, 2008463041, -1344611723, 1109467491, 537923632, -436207846, -34344178, -1076702611, -2117218996, 403442708, 638784309, -1007883217, -1101045791, 899127202, -2008791860, 773265209, -1815821225, 1437050866, -58818942, 2050833735, -932944724, -1168286233, 840505643, -428641387, -1067425632, 0x19818198, -1638969391, -1545806721, 0x44222266, 1412049534, 999329963, 0xB888883, -1941551414, -940642775, 1807268051, 672404540, -1478566279, -1134666014, 369822493, -1378100362, -606019525, 1681011286, 1949973070, 336202270, -1840690725, 201721354, 1210328172, -1201906460, -1614626211, -1110191250, 1135389935, -1000185178, 965841320, 831886756, -739974089, -226920053, -706222286, -1949775805, 1849112409, -630362697, 26054028, -1311386268, -1672589614, 1235855840, -663982924, -1403627782, -202050553, -806688219, -899324497, -193299826, 1202630377, 0x10080818, 1874508501, -260540280, 1243948399, 1546530418, 941366308, 1470539505, 1941222599, -1748580783, -873928669, -1579295364, -395021156, 1042226977, -1773450275, 1639824860, 227249030, 260737669, -529502064, 2084453954, 1907733956, -865704278, -1874310952, 100860677, -134810111, 470683154, -1033805405, 1781871967, -1370007559, 1773779408, 394692241, -1715355304, 974986535, 664706745, -639508168, -336005101, 731420851, 0x22111133, -764843589, -1445340816, 126783113, 865375399, 765172662, 1008606754, 361203602, -907417312, -2016489911, -1437248001, 1344809080, -1512054918, 59542671, 1503764984, 0x9898980, 437062935, 1707065306, -672733647, -2076032314, -798463816, -2109652541, 697932208, 1512910199, 504303377, 2075177163, -1470868228, 0x6DBBBBD6, 739644986};
    private static final int[] AES_Table_2 = new int[]{-1513725085, -2064089988, -1712425097, -1913226373, 0xDFFF2F2, -1110021269, -1310822545, 1418839493, 1348481072, 50462977, -1446090905, 2102799147, 434634494, 1656084439, -431117397, -1695779210, 1167051466, -1658879358, 1082771913, -2013627011, 368048890, -340633255, -913422521, 0xBFBF0F0, -331240019, 1739838676, -44064094, -364531793, -1088185188, -145513308, -1763413390, 1536934080, -1032472649, 484572669, -1371696237, 1783375398, 1517041206, 1098792767, 49674231, 1334037708, 1550332980, -195975771, 886171109, 150598129, -1813876367, 1940642008, 1398944049, 1059722517, 201851908, 1385547719, 1699095331, 1587397571, 674240536, -1590192490, 252314885, -1255171430, 151914247, 908333586, -1692696448, 1038082786, 651029483, 1766729511, -847269198, -1612024459, 454166793, -1642232957, 1951935532, 775166490, 758520603, -1294176658, -290170278, -77881184, -157003182, 1299594043, 1639438038, -830622797, 2068982057, 0x3EDDE3E3, 1901997871, -1760328572, -173649069, 1757008337, 0, 750906861, 1614815264, 535035132, -931548751, -306816165, -1093375382, 1183697867, -647512386, 1265776953, -560706998, -728216500, -391096232, 1250283471, 1807470800, 717615087, -447763798, 384695291, -981056701, -677753523, 0x55663333, -1810791035, -813021883, 283769337, 100925954, -2114027649, -257929136, 1148730428, -1171939425, -481580888, -207466159, -27417693, -1065336768, -1979347057, -1388342638, -1138647651, 1215313976, 82966005, -547111748, -1049119050, 1974459098, 1665278241, 807407632, 451280895, 251524083, 1841287890, 1283575245, 337120268, 891687699, 801369324, -507617441, -1573546089, -863484860, 959321879, 1469301956, -229267545, -2097381762, 1199193405, -1396153244, -407216803, 724703513, -1780059277, -1598005152, -1743158911, -778154161, 2141445340, 0x66442222, 2119445034, -1422159728, -2096396152, -896776634, 700968686, -747915080, 1009259540, 2041044702, -490971554, 487983883, 1991105499, 1004265696, 1449407026, 1316239930, 504629770, -611169975, 168560134, 1816667172, -457679780, 1570751170, 1857934291, -280777556, -1497079198, -1472622191, -1540254315, 936633572, -1947043463, 852879335, 1133234376, 1500395319, -1210421907, -1946055283, 1689376213, -761508274, -532043351, -1260884884, -89369002, 133428468, 634383082, -1345690267, -1896580486, -381178194, 0x18100808, -714097990, -1997506440, 1867130149, 1918643758, 607656988, -245913946, -948718412, 1368901318, 600565992, 2090982877, -1662487436, 557719327, -577352885, -597574211, -2045932661, -2062579062, -1864339344, 1115438654, -999180875, -1429445018, -661632952, 84280067, 33027830, 303828494, -1547542175, 1600795957, -106014889, -798377543, -1860729210, 1486471617, 658119965, -1188585826, 953803233, 334231800, -1288988520, 0x33221111, -1143838359, 1890179545, -1995993458, -1489791852, -1238525029, 574365214, -1844082809, 550103529, 1233637070, -5614251, 2018519080, 2057691103, -1895592820, -128343647, -2146858615, 387583245, -630865985, 836232934, -964410814, -1194301336, -1014873791, -1339450983, 2002398509, 287182607, -881086288, -56077228, -697451589, 975967766};
    private static final int[] AES_Table_3 = new int[]{1671808611, 2089089148, 0x7799EE77, 2072901243, -233963534, 0x6BBDD66B, 1873927791, -984313403, 810573872, 16974337, 1739181671, 729634347, -31856642, -681396777, -1410970197, 1989864566, -901410870, -2103631998, -918517303, 2106063485, -99225606, 1508618841, 1204391495, -267650064, -1377025619, -731401260, -1560453214, -1343601233, -1665195108, -1527295068, 1922491506, -1067738176, -1211992649, -48438787, -1817297517, 644500518, 911895606, 1061256767, -150800905, -867204148, 878471220, -1510714971, -449523227, -251069967, 1905517169, -663508008, 827548209, 356461077, 67897348, -950889017, 593839651, -1017209405, 405286936, -1767819370, 84871685, -1699401830, 118033927, 305538066, -2137318528, -499261470, -349778453, 661212711, -1295155278, 1973414517, 152769033, -2086789757, 745822252, 439235610, 455947803, 1857215598, 1525593178, -1594139744, 1391895634, 994932283, -698239018, -1278313037, 695947817, -482419229, 795958831, -2070473852, 1408607827, -781665839, 0, -315833875, 543178784, -65018884, -1312261711, 1542305371, 1790891114, -884568629, -1093048386, 961245753, 1256100938, 1289001036, 1491644504, -817199665, -798245936, -282409489, -1427812438, -82383365, 1137018435, 1305975373, 0x33556633, -2053893755, 1171229253, -116332039, 33948674, 2139225727, 1357946960, 1011120188, -1615190625, -1461498968, 1374921297, -1543610973, 1086357568, -1886780017, -1834139758, -1648615011, 944271416, -184225291, -1126210628, -1228834890, -629821478, 560153121, 271589392, -15014401, -217121293, -764559406, -850624051, 202643468, 322250259, -332413972, 1608629855, -1750977129, 0x44CC8844, 389623319, -1000893500, -1477290585, 2122513534, 1028094525, 1689045092, 1575467613, 422261273, 1939203699, 1621147744, -2120738431, 1339137615, -595614756, 0x22664422, 712922154, -1867826288, -2004677752, 1187679302, -299251730, -1194103880, 339486740, -562452514, 1591917662, 186455563, -612979237, -532948000, 844522546, 978220090, 169743370, 1239126601, 101321734, 611076132, 1558493276, -1034051646, -747717165, -1393605716, 1655096418, -1851246191, -1784401515, -466103324, 2039214713, -416098841, -935097400, 928607799, 1840765549, -1920204403, -714821163, 1322425422, -1444918871, 1823791212, 1459268694, -200805388, -366620694, 1706019429, 2056189050, -1360443474, 0x8181008, -1160417350, 2022240376, 628050469, 779246638, 472135708, -1494132826, -1261997132, -967731258, -400307224, -579034659, 1956440180, 522272287, 1272813131, -1109630531, -1954148981, -1970991222, 1888542832, 1044544574, -1245417035, 0x66AACC66, 1222152264, 50660867, -167643146, 236067854, 1638122081, 895445557, 1475980887, -1177523783, -2037311610, -1051158079, 489110045, -1632032866, -516367903, -132912136, -1733088360, 0x11332211, 1773916777, -646927911, -1903622258, -1800981612, -1682559589, 505560094, -2020469369, -383727127, -834041906, 0x55FFAA55, 678973480, -545610273, -1936784500, -1577559647, -1988097655, 219617805, -1076206145, -432941082, 1120306242, 1756942440, 1103331905, -1716508263, 762796589, 252780047, -1328841808, 1425844308, -1143575109, 372911126};
    private static final int[] AES_Table_4 = new int[]{1667474886, 2088535288, 0x777799EE, 2071694838, -219017729, 0x6B6BBDD6, 1869591006, -976923503, 808472672, 16843522, 1734846926, 724270422, -16901657, -673750347, -1414797747, 1987484396, -892713585, -2105369313, -909557623, 2105378810, -84273681, 1499065266, 1195886990, -252703749, -1381110719, -724277325, -1566376609, -1347425723, -1667449053, -1532692653, 1920112356, -1061135461, -1212693899, -33743647, -1819038147, 640051788, 909531756, 1061110142, -134806795, -859025533, 875846760, -1515850671, -437963567, -235861767, 1903268834, -656903253, 825316194, 353713962, 67374088, -943238507, 589522246, -1010606435, 404236336, -1768513225, 84217610, -1701137105, 117901582, 303183396, -2139055333, -488489505, -336910643, 656894286, -1296904833, 1970642922, 151591698, -2088526307, 741110872, 437923380, 454765878, 1852748508, 1515908788, -1600062629, 1381168804, 993742198, -690593353, -1280061827, 690584402, -471646499, 791638366, -2071685357, 1398011302, -774805319, 0, -303223615, 538992704, -50585629, -1313748871, 1532751286, 1785380564, -875870579, -1094788761, 960056178, 1246420628, 1280103576, 1482221744, -808498555, -791647301, -269538619, -1431640753, -67430675, 1128514950, 1296947098, 0x33335566, -2054843375, 1162203018, -101117719, 33687044, 2139062782, 1347481760, 1010582648, -1616922075, -1465326773, 1364325282, -1549533603, 1077985408, -1886418427, -1835881153, -1650607071, 943212656, -168491791, -1128472733, -1229536905, -623217233, 555836226, 269496352, -58651, -202174723, -757961281, -842183551, 202118168, 320025894, -320065597, 1600119230, -1751670219, 0x4444CC88, 387397934, -993765485, -1482165675, 2122220284, 1027426170, 1684319432, 1566435258, 421079858, 1936954854, 1616945344, -2122213351, 1330631070, -589529181, 0x22226644, 707427924, -1869567173, -2004319477, 1179044492, -286381625, -1195846805, 336870440, -555845209, 1583276732, 185277718, -606374227, -522175525, 842159716, 976899700, 168435220, 1229577106, 101059084, 606366792, 1549591736, -1027449441, -741118275, -1397952701, 1650632388, -1852725191, -1785355215, -454805549, 2038008818, -404278571, -926399605, 926374254, 1835907034, -1920103423, -707435343, 1313788572, -1448484791, 1819063512, 1448540844, -185333773, -353753649, 1701162954, 2054852340, -1364268729, 0x8081810, -1162160785, 2021165296, 623210314, 774795868, 471606328, -1499008681, -1263220877, -960081513, -387439669, -572687199, 1953799400, 522133822, 1263263126, -1111630751, -1953790451, -1970633457, 1886425312, 1044267644, -1246378895, 0x6666AACC, 1212733584, 50529542, -151649801, 235803164, 1633788866, 892690282, 1465383342, -1179004823, -2038001385, -1044293479, 488449850, -1633765081, -505333543, -117959701, -1734823125, 0x11113322, 1768537042, -640061271, -1903261433, -1802197197, -1684294099, 505291324, -2021158379, -370597687, -825341561, 0x5555FFAA, 673740880, -539002203, -1936945405, -1583220647, -1987477495, 218961690, -1077945755, -421121577, 1111672452, 1751693520, 1094828930, -1717981143, 757954394, 252645662, -1330590853, 1414855848, -1145317779, 370555436};
    private static byte[] const_Rb = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -121};

    private static void AES_128_ExpandKey(int[] expandedKey, byte[] key) {
        int i = 0;
        for (i = 0; i < 4; ++i) {
            expandedKey[i] = (0xFF & key[i * 4]) << 24 ^ (0xFF & key[i * 4 + 1]) << 16 ^ (0xFF & key[i * 4 + 2]) << 8 ^ 0xFF & key[i * 4 + 3];
        }
        for (i = 0; i < 10; ++i) {
            int temp = expandedKey[i * 4 + 3];
            expandedKey[i * 4 + 4] = expandedKey[i * 4] ^ AES_SBox[temp >>> 16 & 0xFF] & 0xFF000000 ^ AES_SBox[temp >>> 8 & 0xFF] & 0xFF0000 ^ AES_SBox[temp & 0xFF] & 0xFF00 ^ AES_SBox[temp >>> 24] & 0xFF ^ AES_128_Rcon[i];
            expandedKey[i * 4 + 5] = expandedKey[i * 4 + 1] ^ expandedKey[i * 4 + 4];
            expandedKey[i * 4 + 6] = expandedKey[i * 4 + 2] ^ expandedKey[i * 4 + 5];
            expandedKey[i * 4 + 7] = expandedKey[i * 4 + 3] ^ expandedKey[i * 4 + 6];
        }
    }

    private static void AES_Encryption(byte[] state, int[] key, byte[] out) {
        int st0 = (0xFF & state[0]) << 24 ^ (0xFF & state[1]) << 16 ^ (0xFF & state[2]) << 8 ^ (0xFF & state[3] ^ key[0]);
        int st1 = (0xFF & state[4]) << 24 ^ (0xFF & state[5]) << 16 ^ (0xFF & state[6]) << 8 ^ (0xFF & state[7] ^ key[1]);
        int st2 = (0xFF & state[8]) << 24 ^ (0xFF & state[9]) << 16 ^ (0xFF & state[10]) << 8 ^ (0xFF & state[11] ^ key[2]);
        int st3 = (0xFF & state[12]) << 24 ^ (0xFF & state[13]) << 16 ^ (0xFF & state[14]) << 8 ^ (0xFF & state[15] ^ key[3]);
        int tmp0 = AES_Table_1[st0 >>> 24] ^ AES_Table_2[st1 >>> 16 & 0xFF] ^ AES_Table_3[st2 >>> 8 & 0xFF] ^ AES_Table_4[st3 & 0xFF] ^ key[4];
        int tmp1 = AES_Table_1[st1 >>> 24] ^ AES_Table_2[st2 >>> 16 & 0xFF] ^ AES_Table_3[st3 >>> 8 & 0xFF] ^ AES_Table_4[st0 & 0xFF] ^ key[5];
        int tmp2 = AES_Table_1[st2 >>> 24] ^ AES_Table_2[st3 >>> 16 & 0xFF] ^ AES_Table_3[st0 >>> 8 & 0xFF] ^ AES_Table_4[st1 & 0xFF] ^ key[6];
        int tmp3 = AES_Table_1[st3 >>> 24] ^ AES_Table_2[st0 >>> 16 & 0xFF] ^ AES_Table_3[st1 >>> 8 & 0xFF] ^ AES_Table_4[st2 & 0xFF] ^ key[7];
        st0 = AES_Table_1[tmp0 >>> 24] ^ AES_Table_2[tmp1 >>> 16 & 0xFF] ^ AES_Table_3[tmp2 >>> 8 & 0xFF] ^ AES_Table_4[tmp3 & 0xFF] ^ key[8];
        st1 = AES_Table_1[tmp1 >>> 24] ^ AES_Table_2[tmp2 >>> 16 & 0xFF] ^ AES_Table_3[tmp3 >>> 8 & 0xFF] ^ AES_Table_4[tmp0 & 0xFF] ^ key[9];
        st2 = AES_Table_1[tmp2 >>> 24] ^ AES_Table_2[tmp3 >>> 16 & 0xFF] ^ AES_Table_3[tmp0 >>> 8 & 0xFF] ^ AES_Table_4[tmp1 & 0xFF] ^ key[10];
        st3 = AES_Table_1[tmp3 >>> 24] ^ AES_Table_2[tmp0 >>> 16 & 0xFF] ^ AES_Table_3[tmp1 >>> 8 & 0xFF] ^ AES_Table_4[tmp2 & 0xFF] ^ key[11];
        tmp0 = AES_Table_1[st0 >>> 24] ^ AES_Table_2[st1 >>> 16 & 0xFF] ^ AES_Table_3[st2 >>> 8 & 0xFF] ^ AES_Table_4[st3 & 0xFF] ^ key[12];
        tmp1 = AES_Table_1[st1 >>> 24] ^ AES_Table_2[st2 >>> 16 & 0xFF] ^ AES_Table_3[st3 >>> 8 & 0xFF] ^ AES_Table_4[st0 & 0xFF] ^ key[13];
        tmp2 = AES_Table_1[st2 >>> 24] ^ AES_Table_2[st3 >>> 16 & 0xFF] ^ AES_Table_3[st0 >>> 8 & 0xFF] ^ AES_Table_4[st1 & 0xFF] ^ key[14];
        tmp3 = AES_Table_1[st3 >>> 24] ^ AES_Table_2[st0 >>> 16 & 0xFF] ^ AES_Table_3[st1 >>> 8 & 0xFF] ^ AES_Table_4[st2 & 0xFF] ^ key[15];
        st0 = AES_Table_1[tmp0 >>> 24] ^ AES_Table_2[tmp1 >>> 16 & 0xFF] ^ AES_Table_3[tmp2 >>> 8 & 0xFF] ^ AES_Table_4[tmp3 & 0xFF] ^ key[16];
        st1 = AES_Table_1[tmp1 >>> 24] ^ AES_Table_2[tmp2 >>> 16 & 0xFF] ^ AES_Table_3[tmp3 >>> 8 & 0xFF] ^ AES_Table_4[tmp0 & 0xFF] ^ key[17];
        st2 = AES_Table_1[tmp2 >>> 24] ^ AES_Table_2[tmp3 >>> 16 & 0xFF] ^ AES_Table_3[tmp0 >>> 8 & 0xFF] ^ AES_Table_4[tmp1 & 0xFF] ^ key[18];
        st3 = AES_Table_1[tmp3 >>> 24] ^ AES_Table_2[tmp0 >>> 16 & 0xFF] ^ AES_Table_3[tmp1 >>> 8 & 0xFF] ^ AES_Table_4[tmp2 & 0xFF] ^ key[19];
        tmp0 = AES_Table_1[st0 >>> 24] ^ AES_Table_2[st1 >>> 16 & 0xFF] ^ AES_Table_3[st2 >>> 8 & 0xFF] ^ AES_Table_4[st3 & 0xFF] ^ key[20];
        tmp1 = AES_Table_1[st1 >>> 24] ^ AES_Table_2[st2 >>> 16 & 0xFF] ^ AES_Table_3[st3 >>> 8 & 0xFF] ^ AES_Table_4[st0 & 0xFF] ^ key[21];
        tmp2 = AES_Table_1[st2 >>> 24] ^ AES_Table_2[st3 >>> 16 & 0xFF] ^ AES_Table_3[st0 >>> 8 & 0xFF] ^ AES_Table_4[st1 & 0xFF] ^ key[22];
        tmp3 = AES_Table_1[st3 >>> 24] ^ AES_Table_2[st0 >>> 16 & 0xFF] ^ AES_Table_3[st1 >>> 8 & 0xFF] ^ AES_Table_4[st2 & 0xFF] ^ key[23];
        st0 = AES_Table_1[tmp0 >>> 24] ^ AES_Table_2[tmp1 >>> 16 & 0xFF] ^ AES_Table_3[tmp2 >>> 8 & 0xFF] ^ AES_Table_4[tmp3 & 0xFF] ^ key[24];
        st1 = AES_Table_1[tmp1 >>> 24] ^ AES_Table_2[tmp2 >>> 16 & 0xFF] ^ AES_Table_3[tmp3 >>> 8 & 0xFF] ^ AES_Table_4[tmp0 & 0xFF] ^ key[25];
        st2 = AES_Table_1[tmp2 >>> 24] ^ AES_Table_2[tmp3 >>> 16 & 0xFF] ^ AES_Table_3[tmp0 >>> 8 & 0xFF] ^ AES_Table_4[tmp1 & 0xFF] ^ key[26];
        st3 = AES_Table_1[tmp3 >>> 24] ^ AES_Table_2[tmp0 >>> 16 & 0xFF] ^ AES_Table_3[tmp1 >>> 8 & 0xFF] ^ AES_Table_4[tmp2 & 0xFF] ^ key[27];
        tmp0 = AES_Table_1[st0 >>> 24] ^ AES_Table_2[st1 >>> 16 & 0xFF] ^ AES_Table_3[st2 >>> 8 & 0xFF] ^ AES_Table_4[st3 & 0xFF] ^ key[28];
        tmp1 = AES_Table_1[st1 >>> 24] ^ AES_Table_2[st2 >>> 16 & 0xFF] ^ AES_Table_3[st3 >>> 8 & 0xFF] ^ AES_Table_4[st0 & 0xFF] ^ key[29];
        tmp2 = AES_Table_1[st2 >>> 24] ^ AES_Table_2[st3 >>> 16 & 0xFF] ^ AES_Table_3[st0 >>> 8 & 0xFF] ^ AES_Table_4[st1 & 0xFF] ^ key[30];
        tmp3 = AES_Table_1[st3 >>> 24] ^ AES_Table_2[st0 >>> 16 & 0xFF] ^ AES_Table_3[st1 >>> 8 & 0xFF] ^ AES_Table_4[st2 & 0xFF] ^ key[31];
        st0 = AES_Table_1[tmp0 >>> 24] ^ AES_Table_2[tmp1 >>> 16 & 0xFF] ^ AES_Table_3[tmp2 >>> 8 & 0xFF] ^ AES_Table_4[tmp3 & 0xFF] ^ key[32];
        st1 = AES_Table_1[tmp1 >>> 24] ^ AES_Table_2[tmp2 >>> 16 & 0xFF] ^ AES_Table_3[tmp3 >>> 8 & 0xFF] ^ AES_Table_4[tmp0 & 0xFF] ^ key[33];
        st2 = AES_Table_1[tmp2 >>> 24] ^ AES_Table_2[tmp3 >>> 16 & 0xFF] ^ AES_Table_3[tmp0 >>> 8 & 0xFF] ^ AES_Table_4[tmp1 & 0xFF] ^ key[34];
        st3 = AES_Table_1[tmp3 >>> 24] ^ AES_Table_2[tmp0 >>> 16 & 0xFF] ^ AES_Table_3[tmp1 >>> 8 & 0xFF] ^ AES_Table_4[tmp2 & 0xFF] ^ key[35];
        tmp0 = AES_Table_1[st0 >>> 24] ^ AES_Table_2[st1 >>> 16 & 0xFF] ^ AES_Table_3[st2 >>> 8 & 0xFF] ^ AES_Table_4[st3 & 0xFF] ^ key[36];
        tmp1 = AES_Table_1[st1 >>> 24] ^ AES_Table_2[st2 >>> 16 & 0xFF] ^ AES_Table_3[st3 >>> 8 & 0xFF] ^ AES_Table_4[st0 & 0xFF] ^ key[37];
        tmp2 = AES_Table_1[st2 >>> 24] ^ AES_Table_2[st3 >>> 16 & 0xFF] ^ AES_Table_3[st0 >>> 8 & 0xFF] ^ AES_Table_4[st1 & 0xFF] ^ key[38];
        tmp3 = AES_Table_1[st3 >>> 24] ^ AES_Table_2[st0 >>> 16 & 0xFF] ^ AES_Table_3[st1 >>> 8 & 0xFF] ^ AES_Table_4[st2 & 0xFF] ^ key[39];
        st0 = AES_SBox[tmp0 >>> 24] & 0xFF000000 ^ AES_SBox[tmp1 >>> 16 & 0xFF] & 0xFF0000 ^ AES_SBox[tmp2 >>> 8 & 0xFF] & 0xFF00 ^ AES_SBox[tmp3 & 0xFF] & 0xFF ^ key[40];
        st1 = AES_SBox[tmp1 >>> 24] & 0xFF000000 ^ AES_SBox[tmp2 >>> 16 & 0xFF] & 0xFF0000 ^ AES_SBox[tmp3 >>> 8 & 0xFF] & 0xFF00 ^ AES_SBox[tmp0 & 0xFF] & 0xFF ^ key[41];
        st2 = AES_SBox[tmp2 >>> 24] & 0xFF000000 ^ AES_SBox[tmp3 >>> 16 & 0xFF] & 0xFF0000 ^ AES_SBox[tmp0 >>> 8 & 0xFF] & 0xFF00 ^ AES_SBox[tmp1 & 0xFF] & 0xFF ^ key[42];
        st3 = AES_SBox[tmp3 >>> 24] & 0xFF000000 ^ AES_SBox[tmp0 >>> 16 & 0xFF] & 0xFF0000 ^ AES_SBox[tmp1 >>> 8 & 0xFF] & 0xFF00 ^ AES_SBox[tmp2 & 0xFF] & 0xFF ^ key[43];
        out[0] = (byte)(st0 >>> 24);
        out[1] = (byte)(st0 >>> 16);
        out[2] = (byte)(st0 >>> 8);
        out[3] = (byte)st0;
        out[4] = (byte)(st1 >>> 24);
        out[5] = (byte)(st1 >>> 16);
        out[6] = (byte)(st1 >>> 8);
        out[7] = (byte)st1;
        out[8] = (byte)(st2 >>> 24);
        out[9] = (byte)(st2 >>> 16);
        out[10] = (byte)(st2 >>> 8);
        out[11] = (byte)st2;
        out[12] = (byte)(st3 >>> 24);
        out[13] = (byte)(st3 >>> 16);
        out[14] = (byte)(st3 >>> 8);
        out[15] = (byte)st3;
    }

    static void AES_128_Encrypt(byte[] state, byte[] key, byte[] encrypted) {
        int[] expandedKey = new int[44];
        Aes128cmac.AES_128_ExpandKey(expandedKey, key);
        Aes128cmac.AES_Encryption(state, expandedKey, encrypted);
    }

    private static void AES_XOR_128(byte[] a, byte[] b, int bPosition, byte[] out) {
        for (int i = 0; i < 16; ++i) {
            out[i] = (byte)(a[i] ^ b[i + bPosition]);
        }
    }

    private static void AES_CMAC_ShiftBitLeft(byte[] input, byte[] output) {
        byte overflow = 0;
        for (int i = 15; i >= 0; --i) {
            output[i] = (byte)(input[i] << 1);
            int n = i;
            output[n] = (byte)(output[n] | overflow);
            overflow = (byte)(0 != (input[i] & 0x80) ? 1 : 0);
        }
    }

    private static void AES_CMAC_GenSubKey(byte[] key, byte[] K1, byte[] K2) {
        byte[] L = new byte[16];
        byte[] Z = new byte[16];
        byte[] temp = new byte[16];
        Aes128cmac.AES_128_Encrypt(Z, key, L);
        if ((L[0] & 0x80) == 0) {
            Aes128cmac.AES_CMAC_ShiftBitLeft(L, K1);
        } else {
            Aes128cmac.AES_CMAC_ShiftBitLeft(L, temp);
            Aes128cmac.AES_XOR_128(temp, const_Rb, 0, K1);
        }
        if ((K1[0] & 0x80) == 0) {
            Aes128cmac.AES_CMAC_ShiftBitLeft(K1, K2);
        } else {
            Aes128cmac.AES_CMAC_ShiftBitLeft(K1, temp);
            Aes128cmac.AES_XOR_128(temp, const_Rb, 0, K2);
        }
    }

    private static void AES_CMAC_Padding(byte[] lastByte, byte[] pad, int length) {
        for (int i = 0; i < 16; ++i) {
            pad[i] = i < length ? lastByte[i] : (i == length ? -128 : 0);
        }
    }

    private static void aesCmacInit(byte[] key, int length, CmacCtx context) {
        Arrays.fill(context.mainKey, (byte)0);
        Arrays.fill(context.key1, (byte)0);
        Arrays.fill(context.key2, (byte)0);
        Arrays.fill(context.X, (byte)0);
        Arrays.fill(context.M_Last, (byte)0);
        Arrays.fill(context.extra, (byte)0);
        context.leftover = 0;
        context.mainKey = new byte[16];
        System.arraycopy(key, 0, context.mainKey, 0, context.mainKey.length);
        Aes128cmac.AES_CMAC_GenSubKey(context.mainKey, context.key1, context.key2);
        context.numOfRounds = (length + 15) / 16;
        if (context.numOfRounds == 0) {
            context.numOfRounds = 1;
            context.flag = 0;
        } else {
            context.flag = length % 16 == 0 ? 1 : 0;
        }
    }

    private static void aesCmacUpdate(CmacCtx ctx, byte[] buffer, int length) {
        block14: {
            byte[] Y;
            int i;
            int currentRounds;
            block13: {
                currentRounds = 0;
                i = 0;
                Y = new byte[16];
                if (0 != ctx.leftover) break block13;
                currentRounds = length / 16;
                for (i = 0; i < currentRounds; ++i) {
                    if (ctx.numOfRounds - 1 == 0 && 0 != ctx.flag && length % 16 == 0) {
                        ctx.extra = new byte[16];
                        System.arraycopy(buffer, 16 * i, ctx.extra, 0, ctx.extra.length);
                        ctx.leftover = 16;
                        return;
                    }
                    Aes128cmac.AES_XOR_128(ctx.X, buffer, 16 * i, Y);
                    Aes128cmac.AES_128_Encrypt(Y, ctx.mainKey, ctx.X);
                    --ctx.numOfRounds;
                }
                if (length % 16 == 0) break block14;
                ctx.leftover = length % 16;
                Arrays.fill(ctx.extra, (byte)0);
                for (i = 0; i < length % 16; ++i) {
                    ctx.extra[i] = buffer[16 * currentRounds + i];
                }
                break block14;
            }
            int newLength = 0;
            byte[] temp = new byte[16];
            byte[] nb = null;
            for (i = 0; i < ctx.leftover; ++i) {
                temp[i] = ctx.extra[i];
            }
            for (i = 0; i < (length >= 16 - ctx.leftover ? 16 - ctx.leftover : length); ++i) {
                temp[i + ctx.leftover] = buffer[i];
            }
            int n = newLength = length > 16 - ctx.leftover ? length - (16 - ctx.leftover) : 0;
            if (newLength != 0) {
                nb = new byte[buffer.length - (16 - ctx.leftover)];
                System.arraycopy(buffer, 16 - ctx.leftover, nb, 0, nb.length);
            }
            currentRounds = newLength / 16;
            if (newLength == 0) {
                ctx.leftover += length;
                for (i = 0; i < ctx.leftover; ++i) {
                    ctx.extra[i] = temp[i];
                }
                return;
            }
            Aes128cmac.AES_XOR_128(ctx.X, temp, 0, Y);
            Aes128cmac.AES_128_Encrypt(Y, ctx.mainKey, ctx.X);
            --ctx.numOfRounds;
            for (i = 0; i < currentRounds; ++i) {
                if (ctx.numOfRounds - 1 == 0 && ctx.flag != 0) {
                    for (int j = 0; j < 16; ++j) {
                        ctx.extra[j] = nb[16 * i + j];
                    }
                    ctx.leftover = 16;
                    return;
                }
                Aes128cmac.AES_XOR_128(ctx.X, nb, 16 * i, Y);
                Aes128cmac.AES_128_Encrypt(Y, ctx.mainKey, ctx.X);
                --ctx.numOfRounds;
            }
            ctx.leftover = newLength % 16;
            if (ctx.leftover != 0) {
                Arrays.fill(ctx.extra, (byte)0);
                for (i = 0; i < ctx.leftover; ++i) {
                    ctx.extra[i] = nb[16 * currentRounds + i];
                }
            }
        }
    }

    private static void aesCmacFinal(CmacCtx ctx, byte[] mac) {
        byte[] Y = new byte[16];
        if (ctx.leftover > 0) {
            if (ctx.leftover < 16) {
                byte[] padded = new byte[16];
                Aes128cmac.AES_CMAC_Padding(ctx.extra, padded, ctx.leftover);
                Aes128cmac.AES_XOR_128(padded, ctx.key2, 0, ctx.M_Last);
            } else {
                Aes128cmac.AES_XOR_128(ctx.extra, ctx.key1, 0, ctx.M_Last);
            }
        }
        Aes128cmac.AES_XOR_128(ctx.X, ctx.M_Last, 0, Y);
        Aes128cmac.AES_128_Encrypt(Y, ctx.mainKey, ctx.X);
        for (int i = 0; i < 16; ++i) {
            mac[i] = ctx.X[i];
        }
    }

    public static void aes128cmacInternal(Blob key, Blob key1, Blob[] dataFragments, int numFragments, byte[] buffer, int bufferSize) {
        int i;
        byte[] sig = new byte[16];
        CmacCtx context = new CmacCtx();
        int size = 0;
        Arrays.fill(buffer, (byte)0);
        for (i = 0; i < numFragments; ++i) {
            if (dataFragments[i].data == null) continue;
            size += dataFragments[i].len;
        }
        Aes128cmac.aesCmacInit(key.data, size, context);
        for (i = 0; i < numFragments; ++i) {
            if (dataFragments[i].data == null || dataFragments[i].len <= 0) continue;
            Aes128cmac.aesCmacUpdate(context, dataFragments[i].data, dataFragments[i].len);
        }
        Aes128cmac.aesCmacFinal(context, sig);
        for (i = 0; i < bufferSize; ++i) {
            buffer[i] = sig[i];
        }
    }
}
