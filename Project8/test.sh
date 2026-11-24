REF=~brylow/cosc4400/Projects
$REF/mjparser testcases/Proj8/sweep.java > sweep.ast
head -3 sweep.ast           

$REF/mjchecker sweep.ast > sweep.types
head -3 sweep.types         

$REF/mjtranslator -riscv sweep.types > test.ir
head -3 test.ir           

$REF/mjassem test.ir > ref.txt
java Assem.Main < test.ir > mine.txt
diff mine.txt ref.txt