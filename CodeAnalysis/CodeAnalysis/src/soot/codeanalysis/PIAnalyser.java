
package soot.codeanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.data.AndroidMethod.CATEGORY;
import soot.jimple.infoflow.android.data.AndroidMethodCategoryComparator;
import soot.jimple.infoflow.sourcesSinks.definitions.MethodSourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.definitions.SourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.definitions.SourceSinkType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.ide.icfg.AbstractJimpleBasedICFG;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.BodyTransformer;
import soot.G;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Transform;
import soot.UnitBox;
import soot.UnitPatchingChain;
import soot.Value;
import soot.ValueBox;
import soot.jbco.jimpleTransformations.GotoInstrumenter;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.Arrays;
import java.util.Stack;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;

import fj.P;
import java_cup.shift_action;
import polyglot.ast.Branch;
import polyglot.ast.If;
import android.Manifest;
import android.R.integer;
import soot.toolkits.graph.UnitGraph;
import soot.jimple.IfStmt;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.tools.CFGViewer;
import soot.util.Chain;
import soot.util.cfgcmd.CFGGraphType;

public class PIAnalyser {
	public static ArrayList<String> classlist = new ArrayList<String>();
	public static List<String> ComponentList=new ArrayList<String>();
	public static List<AnalysisStatement> PendingIntentPoint=new ArrayList<AnalysisStatement>();
	public static List<ApkMethod> PIMethodList=new ArrayList<ApkMethod>();
	public static List<Branchstatement> BranchStmtList=new ArrayList<Branchstatement>();
	public static List<StatementProperties> StmtProList=new ArrayList<StatementProperties>();
	public static Boolean constructor=false;
	public static Boolean HaveReplece=false;
	public static Boolean IntentImplicit=false;
	public static Boolean ApkHasVulnerability=false;
	public static Iterator methodIt;
	public static Unit PendingPointUnit;
	public static Chain<Local> LocalList;
	public static String ApplicationClassName=null;
	public static String PendingIntentMethod=null;
	public static long StartTime;
	public static long EndTime;
	public static long TotalTime;
	public static long StartTimeJimple;
	public static long EndTimeJimple;
	public static long TotalTimeJimple;


	public static void main(String[] args) {
		StartTimeJimple = System.currentTimeMillis();
		String ManifestPath="C:/soot project/apk/insta_hd_v1.0_www.AndroidHa.com/AndroidManifest(insta_hd_v1.0_www.AndroidHa.com).xml";
		ManifestAnalyser Analyser=new ManifestAnalyser(ManifestPath);
		ComponentList=Analyser.getComponents();
		System.out.println( "****     PIAnalyser     ****");
		System.out.println( "apk Component List is : " + ComponentList);
		Options.v().set_android_jars("F:/Android-Sdk/platforms");
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_process_dir(Collections.singletonList("C:/soot project/apk/insta_hd_v1.0_www.AndroidHa.com.apk"));		
		Scene.v().setSootClassPath(Scene.v().getSootClassPath() + ";"			     
				+ "C:/Program Files/Java/jdk1.8.0_74/jre/lib/jce.jar" + ";" 
				+ "F:/Android-Sdk/platforms/android-29/android.jar" + ";" 
				+ "F:/a/u/p/s/b/s/target/sootclasses-trunk-jar-with-dependencies.jar" + ";" 
				+ "C:/Program Files/Java/jdk1.8.0_74/jre/lib/rt.jar" + ";" 
				+ "F:/Android-Sdk/platforms" + ";" 			    
				+ Options.v().process_dir().get(0).toString());
		Options.v().set_keep_line_number(false);
		Options.v().set_prepend_classpath(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_whole_program(true);
		Options.v().set_verbose(false);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_output_dir("C:/soot project/OutputOfSoot");	
		Scene.v().loadNecessaryClasses();				
		PackManager.v().writeOutput();
		DeleteAdditionalClass();
		EndTimeJimple   = System.currentTimeMillis();
		TotalTimeJimple = EndTimeJimple - StartTimeJimple;
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.println("Run Time for apk conversion to Jimple is " + formatter.format((EndTimeJimple - StartTimeJimple) / 1000d) + " seconds");		
		loadClass(); 
	}
	private static void DeleteAdditionalClass() {	
		File dir = new File("C:/soot project/OutputOfSoot");
		List<String> FileList = Arrays.asList(dir.list(
				new FilenameFilter() {
					@Override public boolean accept(File dir, String name) {
						return name.endsWith(".jimple");
					}
				}
				));
		for (int FileNum=0;FileNum<FileList.size();FileNum++){
			Boolean isEqual=false;
			String a=FileList.get(FileNum);
			for (int ComponentNum=0;ComponentNum<ComponentList.size();ComponentNum++){
				String b=ComponentList.get(ComponentNum);
				if (FileList.get(FileNum).contains(ComponentList.get(ComponentNum))){
					isEqual=true;
				}
			}
			if (isEqual==false ){
				File file=new File("C:/soot project/OutputOfSoot/" + FileList.get(FileNum));
				file.delete();
			}
			if (FileList.get(FileNum).contains("$")){
				File file=new File("C:/soot project/OutputOfSoot/" + FileList.get(FileNum));
				file.delete();
			}
		}	
	}	
	private static void loadClass() {	
		StartTime = System.currentTimeMillis();
		G.v().reset();
		Scene.v().setSootClassPath(Scene.v().getSootClassPath() +";C:/soot project/OutputOfSoot");
		Options.v().set_whole_program(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_android_jars("C:/a/Android-Sdk/platforms");
		Options.v().set_src_prec(Options.src_prec_jimple);	
		for (int i=0;i<ComponentList.size();i++)
		{
			String cl=ComponentList.get(i);
			Scene.v().addBasicClass(cl,SootClass.SIGNATURES);	   
			SootClass sootclass=Scene.v().loadClassAndSupport(cl);
			Scene.v().forceResolve(cl, SootClass.BODIES);
			sootclass.setApplicationClass(); 
		}
		Scene.v().loadNecessaryClasses();		
		loadMethods();
	}
	private static void loadMethods(){
		for (final SootClass sclass: Scene.v().getApplicationClasses()) {
			methodIt = sclass.getMethods().iterator();
			while (methodIt.hasNext()) {
				PendingIntentPoint.clear();
				List<Unit> BodyUnit=new ArrayList<Unit>();
				BranchStmtList.clear();
				StmtProList.clear();
				SootMethod sootmethod = (SootMethod)methodIt.next();
				int UnitNumber=-1;
				int IfUnitNumber=0;
				String methodname=sootmethod.getName();
				String sMethod=sootmethod.toString();
				Body body=sootmethod.retrieveActiveBody();
				LocalList=body.getLocals();
				BlockGraph blockGraph = new ExceptionalBlockGraph(body);	
				List<Block> BlockList=blockGraph.getBlocks();
				UnitPatchingChain bodyunitlist=body.getUnits();			
				Iterator unitit=bodyunitlist.iterator();
				while (unitit.hasNext()) {
					Unit value= (Unit)unitit.next();
					Stmt Statement=(Stmt) value;	
					UnitNumber=UnitNumber+1;
					if (Statement.branches() && Statement.getClass().getSimpleName().equals("JIfStmt")){
						StatementPropertiesValue(value,body,sclass,sootmethod);
						Branchstatement BranchStmt=new Branchstatement();
						BranchStmt.BranchUnit=value;
						BranchStmt.BranchUnitNumber=UnitNumber;
						BranchStmt.Type=1;
						JIfStmt IfStatement=(JIfStmt) Statement;
						Stmt ElseStmt=IfStatement.getTarget();
						Unit ElseUnit=(Unit) ElseStmt;
						Unit SuccOfIfStmt=bodyunitlist.getSuccOf(value);	
						BranchStmtList.add(BranchStmt);
						for (int BlockNum=0;BlockNum<BlockList.size();BlockNum++) {
							Block blockCheck=BlockList.get(BlockNum); 
							Unit FirstUnit=blockCheck.getHead();
							if (FirstUnit.equals(ElseUnit) || FirstUnit.equals(SuccOfIfStmt)){
								Iterator blockiter=blockCheck.iterator();
								while (blockiter.hasNext()){
									Unit unitInBlock=(Unit)blockiter.next();
									BranchStmt.BranchBodyUnitList.add(unitInBlock);
									StatementPropertiesValue(unitInBlock,body,sclass,sootmethod);
								}
							}
						}
					}
					if (Statement.branches() && Statement.getClass().getSimpleName().equals("JLookupSwitchStmt"))
					{
						StatementPropertiesValue(value,body,sclass,sootmethod);
						Branchstatement BranchStmt=new Branchstatement();
						BranchStmt.BranchUnit=value;
						BranchStmt.BranchUnitNumber=UnitNumber;
						BranchStmt.Type=2;
						JLookupSwitchStmt SwitchStatement=(JLookupSwitchStmt) Statement;
						List<Unit> SwitchTargetUnit=SwitchStatement.getTargets();
						SwitchTargetUnit.add(SwitchStatement.getDefaultTarget());
						BranchStmtList.add(BranchStmt);
						for (int BlockNum=0;BlockNum<BlockList.size();BlockNum++) {
							Block blockCheck=BlockList.get(BlockNum); 
							Unit FirstUnit=blockCheck.getHead();
							for (int SwitchNum=0;SwitchNum<SwitchTargetUnit.size();SwitchNum++){
								if (FirstUnit.equals(SwitchTargetUnit.get(SwitchNum))){
									Iterator blockiter=blockCheck.iterator();
									while (blockiter.hasNext()){
										Unit unitInBlock=(Unit)blockiter.next();
										BranchStmt.BranchBodyUnitList.add(unitInBlock);
										StatementPropertiesValue(unitInBlock,body,sclass,sootmethod);
									}
								}
							}								
						}
					}
					StatementPropertiesValue(value,body,sclass,sootmethod);
					BodyUnit.add(value);					
				}
				if (!PendingIntentPoint.isEmpty())
				{
					loadBody(sclass,body,PendingIntentPoint);
					System.out.println("---------------------------------------------------------------------------------------------");
				}
			}
		}
		if (ApkHasVulnerability==false)
		{
			System.out.println("This Application does not have any pending intent related vulnerabilities");
		}
		EndTime   = System.currentTimeMillis();
		TotalTime = EndTime - StartTime;
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Run Time for apk Analysis is " + formatter.format((EndTime - StartTime) / 1000d) + " seconds");

	}
	private static void StatementPropertiesValue(Unit value,Body body,SootClass sClass,SootMethod sMethod){
		Boolean Contains=false;
		for (int StmtNum=0;StmtNum<StmtProList.size();StmtNum++){
			StatementProperties StmtP=StmtProList.get(StmtNum);
			if (StmtP.unit.equals(value)){
				Contains=true;
			}
		}
		if (Contains==false){				
			StatementProperties StmtPro=new StatementProperties();
			StmtPro.unit=value;
			StmtPro.UnitNumber=StmtProList.size()-1;
			Iterator defit=value.getDefBoxes().iterator();
			while (defit.hasNext()) {
				ValueBox defunit=(ValueBox)defit.next();
				Value defvalue=defunit.getValue();
				String DefVal=defvalue.toString();
				if (DefVal.contains("[")){
					DefVal=DefVal.substring(0,DefVal.indexOf("["));
				}
				Iterator LocalIt=LocalList.iterator();
				while (LocalIt.hasNext()) {
					Local local=(Local) LocalIt.next();
					if(local.toString().equals(DefVal)){
						StmtPro.DefList.add(DefVal);
						StmtPro.Def=defvalue;
					}
				}			
			}
			Stmt Statement=(Stmt) value;
			if (Statement.containsInvokeExpr()){
				InvokeExpr invokeExpr=Statement.getInvokeExpr();
				List<ValueBox> b=invokeExpr.getUseBoxes();
				for (int g=0;g<b.size();g++){
					ValueBox v= b.get(g);
					if (v.getValue().getClass().getSimpleName().toString().equals("JimpleLocal"))
					{
						String Variable=v.getValue().toString();
						StmtPro.DefList.add(Variable);
					}
				}													
			}
			Iterator useit=value.getUseBoxes().iterator();
			while (useit.hasNext()) {
				ValueBox useunit=(ValueBox)useit.next();
				Value usevalue=useunit.getValue();	
				if(LocalList.contains(usevalue)){
					StmtPro.UseList.add(usevalue.toString());
					StmtPro.useList.add(usevalue);
				}						
			}
			for (int BranchNum=0;BranchNum<BranchStmtList.size();BranchNum++){
				Branchstatement BranchStmt=BranchStmtList.get(BranchNum);
				if (BranchStmt.BranchBodyUnitList.contains(value) ){
					StmtPro.ControlList.add(BranchStmt);
				}							
			}
			if (Statement.containsInvokeExpr())
			{	
				InvokeExpr invokeExpersion=Statement.getInvokeExpr();
				String invokeExpr=invokeExpersion.toString();
				String invokeExprType=invokeExpersion.getType().toString();
				String Context=Statement.toString();
				Context=Context.substring(Context.indexOf("<")+1);
				Context=Context.substring(0,Context.indexOf(":"));								
				if ((invokeExprType.equals("android.app.PendingIntent")) && (invokeExpersion.getMethod().getName().equals("getActivity") || invokeExpersion.getMethod().getName().equals("getService") || invokeExpersion.getMethod().getName().equals("getBroadcast")  || invokeExpersion.getMethod().getName().equals("getActivities")))
				{
					AnalysisStatement analysisStatement=new AnalysisStatement();
					analysisStatement.Body=body;
					//analysisStatement.BodyUnit=BodyUnit;
					String PendingIntentDefBox=null;
					PendingIntentMethod=sMethod.toString();
					CharSequence arg2=invokeExpersion.getArg(2).toString();
					if (!(Statement.getDefBoxes().isEmpty()))
					{
						Iterator defiterator=Statement.getDefBoxes().iterator();
						while (defiterator.hasNext()) {
							ValueBox defunit=(ValueBox)defiterator.next();
							PendingIntentDefBox=defunit.getValue().toString();	    	    	
						}
					}								
					int StmtProIndex=StmtProList.size()-1;							
					analysisStatement.UnitNumber=StmtPro.UnitNumber;
					analysisStatement.StatementPropertiesList=StmtProList;
					analysisStatement.Unit=value;
					analysisStatement.Arg=(String) arg2;
					analysisStatement.Def=PendingIntentDefBox;
					IntentBaseProperties IntentBase=new IntentBaseProperties();
					IntentBase.IntentName=(String) arg2;
					analysisStatement.IntentBaseList.add(IntentBase);
					analysisStatement.sClassName=sClass.toString();
					analysisStatement.sMethodName=sMethod.toString();
					analysisStatement.Type=1;
					PendingIntentPoint.add(analysisStatement);
					if (invokeExpersion.getMethod().getName().equals("getActivities")){
						analysisStatement.MultipleIntentBase=true;
					}
				}							
			}	
			StmtProList.add(StmtPro);
		}
	}
	private static void loadBody(SootClass sootClass,Body MethodBody,List<AnalysisStatement> PendingIntentPoint)
	{
		ApplicationClassName=sootClass.toString();
		HaveReplece=false;
		LocalList=MethodBody.getLocals();
		for(int counter=0;counter<PendingIntentPoint.size();counter++)
		{
			AnalysisStatement analysisStatement=PendingIntentPoint.get(counter);			
			IntentImplicit=false;
			FindAliasing(analysisStatement);
			BackwardSlicing(analysisStatement,"PendingIntent");	
		}
	}
	private static void FindAliasing(AnalysisStatement analysisStatement){
		for (int UnitNum=0;UnitNum<analysisStatement.StatementPropertiesList.size();UnitNum++)
		{			
			StatementProperties StmtPro=(StatementProperties) analysisStatement.StatementPropertiesList.get(UnitNum);
			Stmt Statement=(Stmt) StmtPro.unit;
			String defvalue=null;
			if (!(StmtPro.Def==null)){
				defvalue=StmtPro.Def.toString();
				if (defvalue.equals(analysisStatement.Arg)){																		
					Iterator useIter=StmtPro.useList.iterator();
					while (useIter.hasNext()){
						Value useval=(Value) useIter.next();
						if (LocalList.contains(useval) &&  (!useval.toString().equals(defvalue))){
							analysisStatement.AliasList.add(useval.toString());			
							IntentBaseProperties intentBaseNew=new IntentBaseProperties();
							intentBaseNew.IntentName=useval.toString();
							analysisStatement.IntentBaseList.add(intentBaseNew);
						}
					}
				}
			}
		}
	}
	private static void BackwardSlicing(AnalysisStatement analysisStatement,String TypeSlice){	
		for(int h=0;h<analysisStatement.StatementPropertiesList.size();h++){
			StatementProperties stm=analysisStatement.StatementPropertiesList.get(h);
			stm.ReleventList.clear();
		}
		for (int UnitNum=analysisStatement.UnitNumber;UnitNum>=0;UnitNum--)
		{			
			StatementProperties StmtPro1=analysisStatement.StatementPropertiesList.get(UnitNum);
			StatementProperties StmtPro2=analysisStatement.StatementPropertiesList.get(UnitNum+1);
			if (TypeSlice=="PendingIntent" && StmtPro2.unit.equals(analysisStatement.Unit)){
				StmtPro2.ReleventList=analysisStatement.AliasList;
				StmtPro2.ReleventList.add(analysisStatement.Arg);				
			}
			else if (TypeSlice=="IfStmt" && StmtPro2.unit.equals(analysisStatement.Unit)){
				StmtPro2.ReleventList=StmtPro2.UseList;				
			}
			Boolean IsIntersect=false;
			for (int i=0;i<StmtPro2.ReleventList.size();i++){
				String Var=StmtPro2.ReleventList.get(i);
				if(StmtPro1.DefList.contains(Var)){
					IsIntersect=true;
					StmtPro2.ReleventList.remove(Var);
				}
			}
			if (IsIntersect==true){
				for (int i=0;i<StmtPro2.ReleventList.size();i++){
					StmtPro1.ReleventList.add(StmtPro2.ReleventList.get(i));
				}
				for (int i=0;i<StmtPro1.UseList.size();i++){
					StmtPro1.ReleventList.add(StmtPro1.UseList.get(i).toString());
				}
				analysisStatement.BackwardSlice.add(StmtPro1);
			}
			else if (IsIntersect==false){
				for (int i=0;i<StmtPro2.ReleventList.size();i++){
					StmtPro1.ReleventList.add(StmtPro2.ReleventList.get(i));
				}
			}
		}
		for (int StmtProNum=0;StmtProNum<analysisStatement.BackwardSlice.size();StmtProNum++){
			StatementProperties StmtProperties=analysisStatement.BackwardSlice.get(StmtProNum);
			if(!StmtProperties.ControlList.isEmpty()){
				AnalysisStatement a=new AnalysisStatement();
				Branchstatement BranchStmt=StmtProperties.ControlList.get(0);
				a.UnitNumber=BranchStmt.BranchUnitNumber-1;
				a.Unit=BranchStmt.BranchUnit;
				a.StatementPropertiesList=analysisStatement.StatementPropertiesList;
				if (BranchStmt.BackwardSlicing==false){
					BranchStmt.BackwardSlicing=true;
					BackwardSlicing(a,"IfStmt");
					for (int BackwarsSliceNum=0;BackwarsSliceNum<a.BackwardSlice.size();BackwarsSliceNum++){
						StatementProperties St=a.BackwardSlice.get(BackwarsSliceNum);
						if (!analysisStatement.BackwardSlice.contains(St))
						{
							analysisStatement.BackwardSlice.add(St);
						}
					}
				}
			}
		}
		if (TypeSlice=="PendingIntent")
		{
			IntentBaseAnalysis(analysisStatement);
		}
	}
	private static void IntentBaseAnalysis(AnalysisStatement analysisStatement){		
		for (int unitNum=analysisStatement.BackwardSlice.size()-1;unitNum>=0;unitNum--)
		{
			StatementProperties StmtPro=(StatementProperties) analysisStatement.BackwardSlice.get(unitNum);
			IntentBaseTypeCheck(StmtPro,analysisStatement);											
		}
		VulnerabilityAnalysis(analysisStatement);
	}
	private static void IntentBaseTypeCheck(StatementProperties StmtPro,AnalysisStatement analysisStatement){
		Stmt Statement=(Stmt) StmtPro.unit;	
		for (int j=0;j<analysisStatement.IntentBaseList.size();j++){
			IntentBaseProperties IntentBase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(j);		
			if (Statement.containsInvokeExpr() && StmtPro.UseList.contains(IntentBase.IntentName)){
				InvokeExpr UseInvokeExpr=Statement.getInvokeExpr();
				if ( UseInvokeExpr.getMethod().getName().equals("setClassName") ||  UseInvokeExpr.getMethod().getName().equals("setClass") || UseInvokeExpr.getMethod().getName().equals("setComponent") || UseInvokeExpr.getMethod().getName().equals("setPackage") || UseInvokeExpr.getMethod().getName().equals("setSelector"))
				{
					IntentBase.isExplicit=true;					
					IntentBase.isImplicit=false;
				}					
				if (UseInvokeExpr.getMethod().getName().equals("<init>") && UseInvokeExpr.getType().toString()=="void")
				{
					IntentBase.Constructor=true;
					if(UseInvokeExpr.getArgs().isEmpty()==false){
						int ArgCount=UseInvokeExpr.getArgCount();
						for (int k=0;k<ArgCount;k++){
							String UseInvokeExprArgType=UseInvokeExpr.getArg(k).getType().toString();
							String UseInvokeExprArgName=UseInvokeExpr.getArg(k).toString();
							if (UseInvokeExprArgType.equals("java.lang.Class")){
								IntentBase.isExplicit=true;
								IntentBase.isImplicit=false;
							}							
						}
					}									    		
				}	    	
			}		
		}
	}
	private static void VulnerabilityAnalysis(AnalysisStatement analysisStatement)
	{
		for (int i=0;i<analysisStatement.IntentBaseList.size();i++){
			IntentBaseProperties IntentBase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(i);
			if (IntentBase.isExplicit==false && IntentBase.isImplicit==false && (IntentBase.Constructor==true || (!IntentBase.MethodCallList.isEmpty())))
			{	
				IntentBase.isImplicit=true;				
			}	  		
			if (IntentBase.isExplicit==true){
				System.out.println("There is a pending intent in Block " + IntentBase.BlockNumber + " of  Method " + analysisStatement.sMethodName +  " in class " + analysisStatement.sClassName + " and Base Intent Type is Explicit ");		  
			}

			if (IntentBase.isImplicit==true){
				IntentImplicit=true;
				if (analysisStatement.Type==1){
					System.out.println("There is a pending intent in Block " + IntentBase.BlockNumber + " of Method " +  analysisStatement.sMethodName + " in class " + analysisStatement.sClassName +  " and Base Intent Type is Implicit");
				}
			}		 
		}
		if (IntentImplicit==true && (analysisStatement.Type==1)){
			ForwardSlicing(analysisStatement);
		}
	}
	private static void ForwardSlicing(AnalysisStatement analysisStatement){		    
		for (int UnitNum=analysisStatement.UnitNumber+2;UnitNum<analysisStatement.StatementPropertiesList.size();UnitNum++)
		{			
			StatementProperties StmtPro1=analysisStatement.StatementPropertiesList.get(UnitNum-1);
			if (StmtPro1.unit.equals(analysisStatement.Unit)){
				StmtPro1.ReleventList.clear();
				if (!(analysisStatement.Def==null)){
					StmtPro1.ReleventList.add(analysisStatement.Def.toString());
				}
			}
			StatementProperties StmtPro2=analysisStatement.StatementPropertiesList.get(UnitNum);
			Boolean IsIntersect=false;
			for (int i=0;i<StmtPro1.ReleventList.size();i++){
				String Var=StmtPro1.ReleventList.get(i);
				if(StmtPro2.UseList.contains(Var)){
					IsIntersect=true;
					StmtPro1.ReleventList.remove(Var);
				}
			}
			if (IsIntersect==true){
				for (int i=0;i<StmtPro1.ReleventList.size();i++){
					StmtPro2.ReleventList.add(StmtPro1.ReleventList.get(i));
				}
				for (int i=0;i<StmtPro2.DefList.size();i++){
					StmtPro2.ReleventList.add(StmtPro2.DefList.get(i).toString());
				}
				analysisStatement.ForwardSlice.add(StmtPro2);
			}
			else if (IsIntersect==false){
				for (int i=0;i<StmtPro1.ReleventList.size();i++){
					StmtPro2.ReleventList.add(StmtPro1.ReleventList.get(i));
				}
			}
		}		
		SuccAnalysis(analysisStatement);
	}
	private static void SuccAnalysis(AnalysisStatement analysisStatement){
		for (int unitNum=0;unitNum<analysisStatement.ForwardSlice.size();unitNum++)
		{
			StatementProperties StmtPro=(StatementProperties) analysisStatement.ForwardSlice.get(unitNum);					
			SinkAnalysis(StmtPro,analysisStatement);
		}
	}
	private static void SinkAnalysis(StatementProperties StmtPro,AnalysisStatement analysisStatement)
	{			
		Stmt Statement=(Stmt) StmtPro.unit;
		for (int i=0;i<StmtPro.UseList.size();i++){
			if (StmtPro.UseList.get(i).equals(analysisStatement.Def) ||  analysisStatement.DefRepList.contains(StmtPro.UseList.get(i)))
			{
				if ((!StmtPro.DefList.isEmpty()) && (!Statement.containsInvokeExpr())){
					analysisStatement.DefRepList.add(StmtPro.DefList.get(0).toString());
				}
				if (Statement.containsInvokeExpr())
				{	
					String Context=Statement.toString();
					Context=Context.substring(Context.indexOf("<")+1);
					Context=Context.substring(0,Context.indexOf(":"));				  
					InvokeExpr invokeExpersion=Statement.getInvokeExpr();
					String invokeExprType=invokeExpersion.getType().toString();
					String invokeExprMethodName=invokeExpersion.getMethod().getName().toString();						
					if (invokeExprType.equals("android.content.Intent") && invokeExprMethodName.equals("putExtra"))
					{	
						AnalysisStatement analysisStatement2=new AnalysisStatement();
						String StatementString=Statement.toString();
						StatementString=StatementString.substring(StatementString.indexOf(" ")+1);
						StatementString=StatementString.substring(0,StatementString.indexOf("."));
						analysisStatement2.UnitNumber=StmtPro.UnitNumber;
						analysisStatement2.Unit=StmtPro.unit;
						analysisStatement2.Type=2;
						analysisStatement2.MethodName=invokeExprMethodName;
						analysisStatement2.StatementPropertiesList=analysisStatement.StatementPropertiesList;
						analysisStatement2.Arg=StatementString;
						IntentBaseProperties intentBase=new IntentBaseProperties();
						intentBase.IntentName=StatementString;
						intentBase.Owner="putExtra";
						analysisStatement2.IntentBaseList.add(intentBase);
						FindAliasing(analysisStatement2);
						BackwardSlicing(analysisStatement2,"PendingIntent");
						for (int k=0;k<analysisStatement2.IntentBaseList.size();k++){
							IntentBaseProperties InteneBasePro=new IntentBaseProperties();
							InteneBasePro=(IntentBaseProperties) analysisStatement2.IntentBaseList.get(k);					
							if (InteneBasePro.isImplicit==true){
								VulnerabilityTypeAnalysis(analysisStatement,intentBase); 
							}
						}
					}
					if (Context.equals("android.app.AlarmManager") || Context.equals("android.widget.RemoteViews") || Context.contains("Notification"))
					{														
						IntentBaseProperties intentBase=new IntentBaseProperties();
						intentBase.Owner="SystemService";
						VulnerabilityTypeAnalysis(analysisStatement,intentBase);
					}
				}			  			  
			}		
		}
	}

	private static void VulnerabilityTypeAnalysis(AnalysisStatement analysisStatement,IntentBaseProperties WrapperIntent)
	{
		for (int IntentBaseNum=0;IntentBaseNum<analysisStatement.IntentBaseList.size();IntentBaseNum++){
			IntentBaseProperties IntentBase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(IntentBaseNum);		
			if (IntentBase.isImplicit==true){
				if (WrapperIntent.Owner.equals("putExtra")){
					ApkHasVulnerability=true;
					System.out.println("This Application has Type 2 vulnerability meaning that Implicit pending Intent with Action is wrraped in an implicit Intent in class " + analysisStatement.sClassName + " and Method " + analysisStatement.callerMethodName);
				}			
				else if (WrapperIntent.Owner.equals("SystemService"))
				{
					ApkHasVulnerability=true;
					System.out.println("This Application has Type 1 vulnerability meaning that Implicit pending Intent is send to system service in class " + analysisStatement.sClassName+ " and Method " + analysisStatement.callerMethodName);				
				}
			}
		}
	}	
}	


