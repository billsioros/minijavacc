
import semantic.visitor.*;

import semantic.detail.*;

import semantic.options.*;

import semantic.error.*;

import llvm.visitor.*;

import llvm.detail.*;

import syntaxtree.*;

import java.io.*;

class Main
{
    static private int status = SemanticErrorManager.SUCCESS;

    public static void main(String[] filenames)
    {
        if (filenames.length < 1)
        {
            System.err.println("Usage: java Main [FILE_1] [FILE_2] ... [FILE_N]");

			System.exit(1);
		}

		FileInputStream fis = null;

        for (String filename : filenames)
        {
            File file = new File(filename);

            try
            {
                fis = new FileInputStream(file);

                MiniJavaParser parser = new MiniJavaParser(fis);

                DeclarationVisitor declarationVisitor = new DeclarationVisitor();

                Global global = declarationVisitor.getGlobal();

                StatementVisitor statementVisitor = new StatementVisitor(global);

                LLVMVisitor llvmVisitor = new LLVMVisitor(global);

                Goal goal = parser.Goal();

                String structure = goal.accept(declarationVisitor);

                statementVisitor.visit(declarationVisitor.getPending());

                status = SemanticErrorManager.getStatus();

                if (status == SemanticErrorManager.FAILURE)
                {
                    SemanticErrorManager.flush(file.getName());

                    continue;
                }

                LLVM.open(filename);

                goal.accept(llvmVisitor);

                LLVM.close();

                if (Options.PRINT_PROGRAM_STRUCTURE)
                    System.out.println(structure);
            }
            catch (ParseException ex)
            {
                System.err.println(ex.getMessage());
            }
            catch (FileNotFoundException ex)
            {
                System.err.println(ex.getMessage());
            }
            finally
            {
                try
                {
                    if (fis != null)
                        fis.close();
                }
                catch (IOException ex)
                {
                    System.err.println(ex.getMessage());
                }
            }
        }

        System.exit(status);
	}
}
