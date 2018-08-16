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
var sprintf_js_1 = require("sprintf-js");
var Lint = require("tslint");
var ts = require("typescript");
var Rule = (function (_super) {
    __extends(Rule, _super);
    function Rule() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    Rule.prototype.apply = function (sourceFile) {
        return this.applyWithWalker(new ClassMetadataWalker(sourceFile, this.getOptions()));
    };
    Rule.metadata = {
        ruleName: 'no-conflicting-life-cycle-hooks',
        type: 'maintainability',
        description: 'Ensure that directives not implement conflicting lifecycle hooks.',
        descriptionDetails: 'See more at https://angular.io/api/core/DoCheck#description.',
        rationale: 'A directive typically should not use both DoCheck and OnChanges to respond ' +
            'to changes on the same input, as ngOnChanges will continue to be called when the ' +
            'default change detector detects changes.',
        options: null,
        optionsDescription: 'Not configurable.',
        typescriptOnly: true,
    };
    Rule.FAILURE_STRING = 'Implement DoCheck and OnChanges hooks in class %s is not recommended';
    return Rule;
}(Lint.Rules.AbstractRule));
exports.Rule = Rule;
var hooksPrefix = 'ng';
var lifecycleHooksMethods = [
    'DoCheck',
    'OnChanges'
];
var ClassMetadataWalker = (function (_super) {
    __extends(ClassMetadataWalker, _super);
    function ClassMetadataWalker() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    ClassMetadataWalker.prototype.visitClassDeclaration = function (node) {
        this.validateInterfaces(node);
        this.validateMethods(node);
        _super.prototype.visitClassDeclaration.call(this, node);
    };
    ClassMetadataWalker.prototype.validateInterfaces = function (node) {
        if (!node.heritageClauses) {
            return;
        }
        var interfacesClause = node.heritageClauses.find(function (h) { return h.token === ts.SyntaxKind.ImplementsKeyword; });
        if (!interfacesClause) {
            return;
        }
        var interfaces = interfacesClause.types.map(function (t) {
            return t.expression.name ? t.expression.name.text : t.expression.text;
        });
        var matchesAllHooks = lifecycleHooksMethods.every(function (l) { return interfaces.indexOf(l) !== -1; });
        if (matchesAllHooks) {
            this.addFailureAtNode(node, sprintf_js_1.sprintf.apply(this, [Rule.FAILURE_STRING, node.name.text]));
        }
    };
    ClassMetadataWalker.prototype.validateMethods = function (node) {
        var methodNames = node.members
            .filter(function (m) { return m.kind === ts.SyntaxKind.MethodDeclaration; })
            .map(function (m) { return m.name.text; });
        var matchesAllHooks = lifecycleHooksMethods.every(function (l) {
            return methodNames.indexOf("" + hooksPrefix + l) !== -1;
        });
        if (matchesAllHooks) {
            this.addFailureAtNode(node, sprintf_js_1.sprintf.apply(this, [Rule.FAILURE_STRING, node.name.text]));
        }
    };
    return ClassMetadataWalker;
}(Lint.RuleWalker));
exports.ClassMetadataWalker = ClassMetadataWalker;
