<map version="freeplane 1.9.8">
<!--To view this file, download free mind mapping software Freeplane from https://www.freeplane.org -->
<node TEXT="Qilin&apos;s code structure" LOCALIZED_STYLE_REF="AutomaticLayout.level.root" FOLDED="false" ID="ID_1090958577" CREATED="1409300609620" MODIFIED="1647920853056" LINK="https://github.com/QilinPTA/Qilin"><hook NAME="MapStyle" background="#2e3440" zoom="0.909">
    <properties show_icon_for_attributes="true" edgeColorConfiguration="#808080ff,#ff0000ff,#0000ffff,#00ff00ff,#ff00ffff,#00ffffff,#7c0000ff,#00007cff,#007c00ff,#7c007cff,#007c7cff,#7c7c00ff" show_note_icons="true" associatedTemplateLocation="template:/dark_nord_template.mm" fit_to_viewport="false"/>

<map_styles>
<stylenode LOCALIZED_TEXT="styles.root_node" STYLE="oval" UNIFORM_SHAPE="true" VGAP_QUANTITY="24 pt">
<font SIZE="24"/>
<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="default" ID="ID_671184412" ICON_SIZE="12 pt" FORMAT_AS_HYPERLINK="false" COLOR="#484747" BACKGROUND_COLOR="#eceff4" STYLE="bubble" SHAPE_HORIZONTAL_MARGIN="8 pt" SHAPE_VERTICAL_MARGIN="5 pt" BORDER_WIDTH_LIKE_EDGE="false" BORDER_WIDTH="1.9 px" BORDER_COLOR_LIKE_EDGE="true" BORDER_COLOR="#f0f0f0" BORDER_DASH_LIKE_EDGE="true" BORDER_DASH="SOLID">
<arrowlink SHAPE="CUBIC_CURVE" COLOR="#88c0d0" WIDTH="2" TRANSPARENCY="255" DASH="" FONT_SIZE="9" FONT_FAMILY="SansSerif" DESTINATION="ID_671184412" STARTARROW="DEFAULT" ENDARROW="NONE"/>
<font NAME="SansSerif" SIZE="11" BOLD="false" STRIKETHROUGH="false" ITALIC="false"/>
<edge STYLE="bezier" COLOR="#81a1c1" WIDTH="3" DASH="SOLID"/>
<richcontent CONTENT-TYPE="plain/auto" TYPE="DETAILS"/>
<richcontent TYPE="NOTE" CONTENT-TYPE="plain/auto"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.details" BORDER_WIDTH="1.9 px">
<edge STYLE="bezier" COLOR="#81a1c1" WIDTH="3"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.attributes" COLOR="#eceff4">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.note" COLOR="#000000" BACKGROUND_COLOR="#ebcb8b">
<icon BUILTIN="clock2"/>
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.floating" COLOR="#484747">
<edge STYLE="hide_edge"/>
<cloud COLOR="#f0f0f0" SHAPE="ROUND_RECT"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.selection" COLOR="#e5e9f0" BACKGROUND_COLOR="#5e81ac" BORDER_COLOR_LIKE_EDGE="false" BORDER_COLOR="#5e81ac"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.user-defined" POSITION="right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="styles.important" ID="ID_779275544" BORDER_COLOR_LIKE_EDGE="false" BORDER_COLOR="#bf616a">
<icon BUILTIN="yes"/>
<arrowlink COLOR="#bf616a" TRANSPARENCY="255" DESTINATION="ID_779275544"/>
<font SIZE="14"/>
</stylenode>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.AutomaticLayout" POSITION="right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="AutomaticLayout.level.root" COLOR="#ffffff" BACKGROUND_COLOR="#484747" STYLE="bubble" SHAPE_HORIZONTAL_MARGIN="10 pt" SHAPE_VERTICAL_MARGIN="10 pt">
<font NAME="Ubuntu" SIZE="18"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,1" COLOR="#eceff4" BACKGROUND_COLOR="#d08770" STYLE="bubble" SHAPE_HORIZONTAL_MARGIN="8 pt" SHAPE_VERTICAL_MARGIN="5 pt">
<font NAME="Ubuntu" SIZE="16"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,2" COLOR="#3b4252" BACKGROUND_COLOR="#ebcb8b">
<font SIZE="14"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,3" COLOR="#2e3440" BACKGROUND_COLOR="#a3be8c">
<font SIZE="12"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,4" COLOR="#2e3440" BACKGROUND_COLOR="#b48ead">
<font SIZE="11"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,5" BACKGROUND_COLOR="#81a1c1">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,6" BACKGROUND_COLOR="#88c0d0">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,7" BACKGROUND_COLOR="#8fbcbb">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,8" BACKGROUND_COLOR="#d8dee9">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,9" BACKGROUND_COLOR="#e5e9f0">
<font SIZE="9"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,10" BACKGROUND_COLOR="#eceff4">
<font SIZE="9"/>
</stylenode>
</stylenode>
</stylenode>
</map_styles>
</hook>
<hook NAME="accessories/plugins/AutomaticLayout.properties" VALUE="ALL"/>
<font BOLD="true"/>
<node TEXT="qilin.core" POSITION="right" ID="ID_1772126643" CREATED="1646005888230" MODIFIED="1647652582456" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.core">
<node TEXT="core" ID="ID_1395244479" CREATED="1647659502639" MODIFIED="1647670719209">
<cloud COLOR="#ffcccc" SHAPE="ARC"/>
<node TEXT="PAG" ID="ID_501194817" CREATED="1647659515930" MODIFIED="1647671306601" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.core/src/qilin/core/pag"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      &nbsp;The pointer assignment graph.
    </p>
  </body>
</html></richcontent>
<node TEXT="MethodPAG" ID="ID_572844518" CREATED="1647661255893" MODIFIED="1647661284986" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/MethodPAG.java"/>
<node TEXT="PAG Nodes" ID="ID_255421313" CREATED="1647667502935" MODIFIED="1647846268012"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      We have defined a set of PAG nodes with the same names, if possible, as those in &quot;soot/jimple/spark/pag/&quot;.
    </p>
  </body>
</html></richcontent>
<node TEXT="Object Node" ID="ID_1478479343" CREATED="1647668108875" MODIFIED="1647670095943">
<node TEXT="StringConstantNode.java" ID="ID_1527804207" CREATED="1647668211657" MODIFIED="1647674547673" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/StringConstantNode.java"/>
<node TEXT="ClassConstantNode.java" ID="ID_1532855451" CREATED="1647668255574" MODIFIED="1647674522994" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/ClassConstantNode.java"/>
<node TEXT="AllocNode.java" ID="ID_1107064112" CREATED="1647668266991" MODIFIED="1647674503474" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/AllocNode.java"/>
<node TEXT="ContextAllocNode.java" ID="ID_1814697921" CREATED="1647668279749" MODIFIED="1647674567183" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/ContextAllocNode.java"/>
</node>
<node TEXT="Pointer Node" ID="ID_464521819" CREATED="1647668167518" MODIFIED="1647668185018">
<node TEXT="GlobalVarNode.java" ID="ID_1167842612" CREATED="1647668462332" MODIFIED="1647674580499" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/GlobalVarNode.java"/>
<node TEXT="LocalVarNode.java" ID="ID_376326985" CREATED="1647668431978" MODIFIED="1647674596876" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/LocalVarNode.java"/>
<node TEXT="FieldValNode.java" ID="ID_421583550" CREATED="1647668635436" MODIFIED="1647674653657" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/FieldValNode.java"/>
<node TEXT="ContextField.java" ID="ID_1344198037" CREATED="1647668712835" MODIFIED="1647674673448" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/ContextField.java"/>
<node TEXT="ContextVarNode.java" ID="ID_704597293" CREATED="1647668725288" MODIFIED="1647674700027" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/pag/ContextVarNode.java"/>
</node>
</node>
</node>
<node TEXT="CallgraphBuilder" ID="ID_424767675" CREATED="1647660598024" MODIFIED="1647846292567" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/builder/CallGraphBuilder.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      It provides an API for handling virtual call dispatch.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="ExceptionHandler" ID="ID_56400937" CREATED="1647661035059" MODIFIED="1647846307054" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/builder/ExceptionHandler.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      It provides an API for exception dispatch along the exception-catch-links.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="CorePTA.java" ID="ID_869430707" CREATED="1647661467503" MODIFIED="1647920840016" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/CorePTA.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This defines an abstract pointer analysis. You can define a concrete pointer analysis (as a subclass) by defining Qilin's three context-sensitivity-controlling parameters.
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="PTA Solver" ID="ID_1883478738" CREATED="1647661044020" MODIFIED="1647846428981" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/core/solver/Solver.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Qilin's incremental worklist-based constraint solver for supporting pointer analyses with fine-grained context-sensitivity. For more details, please refer to Section 3.3 and Algorithm 1 of our ECOOP'2022 paper.
    </p>
  </body>
</html></richcontent>
<node TEXT="incremental sets" ID="ID_971568666" CREATED="1647671121658" MODIFIED="1647846451622" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.core/src/qilin/core/sets"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      These classes are modified from their corresponding ones in &quot;soot/jimple/spark/sets&quot;.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Feature Handlers" ID="ID_753594539" CREATED="1647661054586" MODIFIED="1647661074671">
<node TEXT="native codes" ID="ID_297298171" CREATED="1647661076616" MODIFIED="1647661112345" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.core/src/qilin/core/natives"/>
<node TEXT="reflections" ID="ID_1091308165" CREATED="1647661087010" MODIFIED="1647846545710" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.core/src/qilin/core/reflection"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      We currently discover the reflective targets in a program by using TAMIFLEX before performing pointer analysis on the program. You can design your own reflection handlers here.
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="parameters" ID="ID_1858623953" CREATED="1647659518456" MODIFIED="1647920669687" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.core/src/qilin/parm"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This package includes a set of built-in instantiations of Qilin's three context-sensitivity-controlling parameters.
    </p>
  </body>
</html>
</richcontent>
<node TEXT="Context Constructors" ID="ID_588844366" CREATED="1647659534078" MODIFIED="1647920601559"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A context constructor describes how to construct a new context for a method.
    </p>
  </body>
</html>
</richcontent>
<node TEXT="CallsiteCtxConstructor.java" ID="ID_1516173377" CREATED="1647671830935" MODIFIED="1647675612767" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/ctxcons/CallsiteCtxConstructor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (2) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="ObjCtxConstructor.java" ID="ID_337829122" CREATED="1647671848487" MODIFIED="1647675627994" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/ctxcons/ObjCtxConstructor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (3) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="TypeCtxConstructor.java" ID="ID_919649522" CREATED="1647671851616" MODIFIED="1647675647295" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/ctxcons/TypeCtxConstructor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (4) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="HybObjCtxConstructor.java" ID="ID_1643786777" CREATED="1647671852073" MODIFIED="1647675674200" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/ctxcons/HybObjCtxConstructor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (5) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="HybTypeCtxConstructor.java" ID="ID_571039175" CREATED="1647671852808" MODIFIED="1647846658829" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/ctxcons/HybTypeCtxConstructor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Refer to Selective 2-type-sensitive with 1-context-sensitive heap hybrid (s-2type+H) in &quot;Hybrid context-sensitivity for Points-to Analysis&quot; (PLDI'13).
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="InsensCtxConstructor.java" ID="ID_322683104" CREATED="1647671905869" MODIFIED="1647675688645" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/ctxcons/InsensCtxConstructor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (1) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Context Selector" ID="ID_569028628" CREATED="1647659685160" MODIFIED="1647675499809"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Context selector selects some context elements from the context of a method to form a new context for a variable declared (or object allocated ) in the method.
    </p>
  </body>
</html></richcontent>
<node TEXT="UniformSelector.java" ID="ID_412459172" CREATED="1647671920646" MODIFIED="1647676009906" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/UniformSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (6) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="InsenSelector.java" ID="ID_195900117" CREATED="1647672021481" MODIFIED="1647846675614" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/InsenSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This context selector is defined for context-insensitive pointer analysis. For all variables and objects, it returns an empty context.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="HeuristicSelector.java" ID="ID_1360565120" CREATED="1647672032305" MODIFIED="1647676058173" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/HeuristicSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (8) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="FullMethodLvSelector.java" ID="ID_1649067982" CREATED="1647672265240" MODIFIED="1647920582271" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/FullMethodLvSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This context selector is used in Data-driven pointer analysis (OOPSLA'2017). Every method can use contexts of different context lengths.
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="PartialMethodLvSelector.java" ID="ID_974978017" CREATED="1647672303618" MODIFIED="1647676088703" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/PartialMethodLvSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (9) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="VarLvSelector.java" ID="ID_1997357926" CREATED="1647672353818" MODIFIED="1647847026851" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/VarLvSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This context selector allows all variables and objects to have have their own context abstractions (with, e.g., different context lengths) in the most general case.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="PartialVarSelector.java" ID="ID_537538141" CREATED="1647672374351" MODIFIED="1647676155134" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/PartialVarSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (10) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="DebloatingSelector.java" ID="ID_117423742" CREATED="1647672387175" MODIFIED="1647846893093" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/DebloatingSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This context selector is designed for <a href="https://dl.acm.org/doi/abs/10.1145/3062341.3062360">Mahjong</a>&nbsp;and <a href="https://doi.org/10.1109/ASE51524.2021.9678880">Context debloating technique</a>&nbsp;introduced in ASE'21 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="BeanSelector.java" ID="ID_1964196985" CREATED="1647672404521" MODIFIED="1647920504705" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/BeanSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This context selector is specially designed for <a href="https://link.springer.com/chapter/10.1007/978-3-662-53413-7_24">BEAN</a>.
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="PipelineSelector.java" ID="ID_1475286544" CREATED="1647672417325" MODIFIED="1647847074819" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/select/PipelineSelector.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This context selector is defined in terms of two other context selectors. See the formal definition in Section 4.4 of our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Heap Abstractor" ID="ID_1960453114" CREATED="1647660194949" MODIFIED="1647920361623"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A heap abstractor defines the heap abstraction for an object.
    </p>
  </body>
</html>
</richcontent>
<node TEXT="AllocSiteAbstractor.java" ID="ID_1511252923" CREATED="1647672460520" MODIFIED="1647676786898" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/heapabst/AllocSiteAbstractor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (11) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="HeuristicAbstractor.java" ID="ID_908254535" CREATED="1647672483085" MODIFIED="1647676832747" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/heapabst/HeuristicAbstractor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (12) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="MahjongAbstractor.java" ID="ID_1837404566" CREATED="1647672497679" MODIFIED="1647676854447" LINK="https://github.com/QilinPTA/Qilin/blob/main/qilin.core/src/qilin/parm/heapabst/MahjongAbstractor.java"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Equation (13) in our ECOOP'22 paper.
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="PTA evaluator" ID="ID_1337934979" CREATED="1647659537385" MODIFIED="1647920343048" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.core/src/qilin/stat"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Records many statistics of the pointer analysis for a given benchmark.
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node TEXT="qilin.pta" POSITION="left" ID="ID_1631542550" CREATED="1646005939111" MODIFIED="1647666262688" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.pta"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If you want to create your own pointer analysis, please put your code under this sub-project.
    </p>
  </body>
</html></richcontent>
<node TEXT="driver" ID="ID_204935873" CREATED="1647655862946" MODIFIED="1647656464559" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.pta/src/driver">
<node TEXT="Main.java" ID="ID_1127153459" CREATED="1647655980522" MODIFIED="1647656138724"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Qilin's command line entry class.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="PTAOption.java" ID="ID_168413891" CREATED="1647656246813" MODIFIED="1647656308878"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Qilin command line options
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="PTAPattern.java" ID="ID_1870669745" CREATED="1647656315882" MODIFIED="1647920319752"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Pointer analysis patterns, which tell Qilin which PTA to invoke.
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node TEXT="concrete PTAs" ID="ID_1258139841" CREATED="1647655869372" MODIFIED="1647920276256"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      PTA implementations are stored under the &quot;tools/&quot; directory while their corresponding pre-analyses (if any) are stored under the &quot;toolkits&quot; directory.
    </p>
  </body>
</html>
</richcontent>
<node TEXT="Spark.java" ID="ID_796120006" CREATED="1647656835629" MODIFIED="1647920249720"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This performs the standard context-insensitive pointer analysis. The PTA pattern is &quot;insens&quot;.
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="ObjectSensPTA.java" ID="ID_1767122784" CREATED="1647656797502" MODIFIED="1647847243627"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      k-OBJ, the PTA pattern is &quot;ko&quot;, k = {1, 2, 3, ...}
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="CallSiteSensPTA.java" ID="ID_1575094612" CREATED="1647656749263" MODIFIED="1647847259162"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      k-CFA, the PTA pattern is &quot;kc&quot;, k = {1,2,3,...}
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="TypeSensPTA.java" ID="ID_468923102" CREATED="1647656889240" MODIFIED="1647847278882"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      k-Type, the PTA pattern is &quot;kt&quot;, k = {1, 2, 3, ...}
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="HybridObjectSensPTA.java" ID="ID_147211068" CREATED="1647656921285" MODIFIED="1647847270899"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Hybrid kOBJ. The PTA pattern is &quot;kh&quot;, k = {1, 2, 3, ...}
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="DebloatedPTA.java" ID="ID_30904192" CREATED="1647657019222" MODIFIED="1647920150993"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Context debloating for Object-sensitivity. The PTA pattern can be {ko, Z-ko, E-ko} + &quot;-cd&quot; respectively for debloating kOBJ, Zipper-guided kOBJ, and Eagle-guided kOBJ.
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="TurnerPTA.java" ID="ID_1448083641" CREATED="1647657038375" MODIFIED="1647920208160"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Turner-guided k-OBJ. The PTA pattern is &quot;T-ko&quot;, k = {1, 2, 3, ...}
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="MahjongPTA.java" ID="ID_1735121902" CREATED="1647657046994" MODIFIED="1647847412715"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Mahjong-guided context-sensitive pointer analysis. The PTA pattern can be one of {M-kc, M-ko}, k = {1, 2, 3, ...}
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="EaglePTA.java" ID="ID_462522753" CREATED="1647657869775" MODIFIED="1647847312794"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Eagle-guided kOBJ. The PTA pattern is &quot;E-ko&quot;, k = {1, 2, 3, ...}
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="ZipperPTA.java" ID="ID_1279361768" CREATED="1647657064838" MODIFIED="1647657082404"/>
<node TEXT="BeanPTA.java" ID="ID_1561150412" CREATED="1647656716483" MODIFIED="1647847334050"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Bean-guided 2-object-sensitive pointer analysis. The PTA pattern is &quot;B-2o&quot;.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="TunnelingPTA.java" ID="ID_1745123289" CREATED="1647657115381" MODIFIED="1647847375907"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Context tunneling guided pointer analysis. The PTA patterns can be one of {'t-1c', t-2t, t-2o, t-2h}.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="DataDrivenPTA.java" ID="ID_239532551" CREATED="1647657084862" MODIFIED="1647920123337"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Data-driven point.er analysis. The PTA patterns can be {D-2o, D-2c, D-2h, D-2ht}
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
</node>
<node TEXT="qilin.microben" POSITION="left" ID="ID_31840620" CREATED="1647652325626" MODIFIED="1647847395298" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.microben"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      About 100 unit test cases for testing Qilin. If you want to add more unit test cases, please put your code here.
    </p>
  </body>
</html></richcontent>
<node TEXT="context sensitivity" ID="ID_1413269164" CREATED="1647655509894" MODIFIED="1647655732940" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.microben/src/qilin/microben/context">
<node TEXT="k-CFA" ID="ID_1035661080" CREATED="1647655667941" MODIFIED="1647655676286"/>
<node TEXT="k-OBJ" ID="ID_680749913" CREATED="1647655678067" MODIFIED="1647655685126"/>
<node TEXT="k-Hybrid" ID="ID_15877272" CREATED="1647655686262" MODIFIED="1647655696183"/>
<node TEXT="collections" ID="ID_615689229" CREATED="1647655709522" MODIFIED="1647655713973"/>
</node>
<node TEXT="flow sensitivity" ID="ID_1587291148" CREATED="1647655539393" MODIFIED="1647655653784"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      not supported yet by Qilin.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="general test cases" ID="ID_309811131" CREATED="1647655550300" MODIFIED="1647655805957" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.microben/src/qilin/microben/core">
<node TEXT="array" ID="ID_1914348410" CREATED="1647655749476" MODIFIED="1647655754776"/>
<node TEXT="assign" ID="ID_1326871225" CREATED="1647655758694" MODIFIED="1647655761156"/>
<node TEXT="call" ID="ID_149783059" CREATED="1647655761601" MODIFIED="1647655763809"/>
<node TEXT="clinit" ID="ID_1718826441" CREATED="1647655764279" MODIFIED="1647655766737"/>
<node TEXT="exception" ID="ID_691618412" CREATED="1647655767206" MODIFIED="1647655770206"/>
<node TEXT="field" ID="ID_193574137" CREATED="1647655770643" MODIFIED="1647655772912"/>
<node TEXT="global" ID="ID_1528638747" CREATED="1647655773445" MODIFIED="1647655776378"/>
<node TEXT="natives" ID="ID_586715466" CREATED="1647655776826" MODIFIED="1647655779838"/>
<node TEXT="reflog" ID="ID_1885535317" CREATED="1647655780272" MODIFIED="1647655782901"/>
</node>
</node>
<node TEXT="artifact" POSITION="left" ID="ID_1273262914" CREATED="1647654563704" MODIFIED="1647654590263" LINK="https://github.com/QilinPTA/Qilin/tree/main/artifact">
<node TEXT="qilin.py" ID="ID_608583388" CREATED="1647654635635" MODIFIED="1647847474090" LINK="https://github.com/QilinPTA/Qilin/blob/main/artifact/qilin.py"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The driver script of Qilin.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="run.py" ID="ID_463876181" CREATED="1647654640501" MODIFIED="1647920099811" LINK="https://github.com/QilinPTA/Qilin/blob/main/artifact/run.py"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The driver script for using Qilin to analyse benchmarks.
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="Qilin-version-SNAPSHOT.jar" ID="ID_519723870" CREATED="1647671574050" MODIFIED="1647671740254"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The Qilin package with dependencies. It is compiled by JDK16+ and thus requires a recent JDK to run.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="benchmarks" ID="ID_1963330790" CREATED="1647654641052" MODIFIED="1647655073302" LINK="https://github.com/QilinPTA/benchmarks">
<node TEXT="JREs" ID="ID_1900225967" CREATED="1647655111019" MODIFIED="1647655117111">
<node TEXT="jre1.6.0_45" ID="ID_1398133851" CREATED="1647655137165" MODIFIED="1647655152566"/>
<node TEXT="jre1.8.0_312" ID="ID_617881107" CREATED="1647655157540" MODIFIED="1647655164931"/>
</node>
<node TEXT="dacapo2006" ID="ID_78619906" CREATED="1647655118603" MODIFIED="1647847453810"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The old version of DaCaPo benchmarks from the artifact of Mahjong. It should be used together with jre1.6.0_45.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="dacapo2018" ID="ID_247066141" CREATED="1647655119029" MODIFIED="1647666913616"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The latest version of DaCapo (revision 6cf0380) compiled by us. This set of benchmarks should work together with jre1.8.0_312.
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="qilin.util" POSITION="right" ID="ID_1394854654" CREATED="1647652314737" MODIFIED="1647671415084" LINK="https://github.com/QilinPTA/Qilin/tree/main/qilin.util"><richcontent TYPE="NOTE" CONTENT-TYPE="xml/">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This subproject includes a set of utility classes.
    </p>
  </body>
</html></richcontent>
</node>
</node>
</map>
