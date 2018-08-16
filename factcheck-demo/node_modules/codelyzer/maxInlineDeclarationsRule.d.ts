import * as Lint from 'tslint';
import { IOptions } from 'tslint';
import * as ts from 'typescript';
import { ComponentMetadata } from './angular/metadata';
import { NgWalker } from './angular/ngWalker';
export declare class Rule extends Lint.Rules.AbstractRule {
    static metadata: Lint.IRuleMetadata;
    private readonly templateLinesLimit;
    private readonly stylesLinesLimit;
    constructor(options: IOptions);
    apply(sourceFile: ts.SourceFile): Lint.RuleFailure[];
}
export declare class MaxInlineDeclarationsValidator extends NgWalker {
    private rule;
    private templateLinesLimit;
    private stylesLinesLimit;
    private newLineRegExp;
    constructor(sourceFile: ts.SourceFile, rule: Rule, templateLinesLimit: number, stylesLinesLimit: number);
    protected visitNgComponent(metadata: ComponentMetadata): void;
    private validateInlineTemplate(metadata);
    private hasInlineTemplate(metadata);
    private getTemplateLinesCount(metadata);
    private validateInlineStyles(metadata);
    private hasInlineStyles(metadata);
    private getInlineStylesLinesCount(metadata);
}
