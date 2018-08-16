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
var ngWalker_1 = require("./angular/ngWalker");
var Rule = (function (_super) {
    __extends(Rule, _super);
    function Rule() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    Rule.prototype.apply = function (sourceFile) {
        return this.applyWithWalker(new ExpressionCallMetadataWalker(sourceFile, this.getOptions()));
    };
    Rule.metadata = {
        ruleName: 'no-life-cycle-call',
        type: 'maintainability',
        description: 'Disallows explicit calls to lifecycle hooks.',
        rationale: 'Explicit calls to lifecycle hooks could be confusing. Invoke lifecycle hooks is the responsability of Angular.',
        options: null,
        optionsDescription: 'Not configurable.',
        typescriptOnly: true,
    };
    Rule.FAILURE_STRING = 'Avoid explicitly calls to lifecycle hooks in class "%s"';
    return Rule;
}(Lint.Rules.AbstractRule));
exports.Rule = Rule;
exports.lifecycleHooksMethods = new Set([
    'ngAfterContentChecked',
    'ngAfterContentInit',
    'ngAfterViewChecked',
    'ngAfterViewInit',
    'ngDoCheck',
    'ngOnChanges',
    'ngOnDestroy',
    'ngOnInit'
]);
var ExpressionCallMetadataWalker = (function (_super) {
    __extends(ExpressionCallMetadataWalker, _super);
    function ExpressionCallMetadataWalker() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    ExpressionCallMetadataWalker.prototype.visitCallExpression = function (node) {
        this.validateCallExpression(node);
        _super.prototype.visitCallExpression.call(this, node);
    };
    ExpressionCallMetadataWalker.prototype.validateCallExpression = function (node) {
        var name = node.expression.name;
        if (!name || !exports.lifecycleHooksMethods.has(name.text)) {
            return;
        }
        var currentNode = node;
        while (currentNode.parent.parent) {
            currentNode = currentNode.parent;
        }
        var failureConfig = [Rule.FAILURE_STRING, currentNode.name.text];
        this.addFailureAtNode(node, sprintf_js_1.sprintf.apply(this, failureConfig));
    };
    return ExpressionCallMetadataWalker;
}(ngWalker_1.NgWalker));
exports.ExpressionCallMetadataWalker = ExpressionCallMetadataWalker;
