"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
var Lint = require("tslint");
var ts = require("typescript");
var ngWalker_1 = require("./angular/ngWalker");
var Rule = (function (_super) {
    __extends(Rule, _super);
    function Rule() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    Rule.prototype.apply = function (sourceFile) {
        return this.applyWithWalker(new OutputMetadataWalker(sourceFile, this.getOptions()));
    };
    Rule.metadata = {
        description: 'Prefer to declare `@Output` as readonly since they are not supposed to be reassigned.',
        options: null,
        optionsDescription: 'Not configurable.',
        rationale: '',
        ruleName: 'prefer-output-readonly',
        type: 'maintainability',
        typescriptOnly: true,
    };
    Rule.FAILURE_STRING = 'Prefer to declare `@Output` as readonly since they are not supposed to be reassigned';
    return Rule;
}(Lint.Rules.AbstractRule));
exports.Rule = Rule;
var OutputMetadataWalker = (function (_super) {
    __extends(OutputMetadataWalker, _super);
    function OutputMetadataWalker() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    OutputMetadataWalker.prototype.visitNgOutput = function (property, output, args) {
        if (property.modifiers && property.modifiers.some(function (m) { return m.kind === ts.SyntaxKind.ReadonlyKeyword; })) {
            return;
        }
        var className = property.parent.name.getText();
        var memberName = property.name.getText();
        this.addFailureAtNode(property.name, Rule.FAILURE_STRING);
        _super.prototype.visitNgOutput.call(this, property, output, args);
    };
    return OutputMetadataWalker;
}(ngWalker_1.NgWalker));
exports.OutputMetadataWalker = OutputMetadataWalker;
