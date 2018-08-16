import * as Lint from 'tslint';
import * as ts from 'typescript';
import { DirectiveMetadata } from './angular/metadata';
import { NgWalker } from './angular/ngWalker';
export declare class Rule extends Lint.Rules.AbstractRule {
    static metadata: Lint.IRuleMetadata;
    static FAILURE_STRING: string;
    apply(sourceFile: ts.SourceFile): Lint.RuleFailure[];
}
export declare class InputMetadataWalker extends NgWalker {
    private directiveSelector;
    visitNgDirective(metadata: DirectiveMetadata): void;
    visitNgInput(property: ts.PropertyDeclaration, input: ts.Decorator, args: string[]): void;
}
