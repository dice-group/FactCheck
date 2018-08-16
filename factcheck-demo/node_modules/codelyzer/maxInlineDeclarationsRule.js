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
var ngWalker_1 = require("./angular/ngWalker");
var Rule = (function (_super) {
    __extends(Rule, _super);
    function Rule(options) {
        var _this = _super.call(this, options) || this;
        _this.templateLinesLimit = 3;
        _this.stylesLinesLimit = 3;
        if (options.ruleArguments.length > 1) {
            var args = options.ruleArguments[1];
            if (args.template > -1) {
                _this.templateLinesLimit = args.template;
            }
            if (args.styles > -1) {
                _this.stylesLinesLimit = args.styles;
            }
        }
        return _this;
    }
    Rule.prototype.apply = function (sourceFile) {
        return this.applyWithWalker(new MaxInlineDeclarationsValidator(sourceFile, this, this.templateLinesLimit, this.stylesLinesLimit));
    };
    Rule.metadata = {
        ruleName: 'max-inline-declarations',
        type: 'maintainability',
        description: 'Disallows having too many lines in inline template or styles. Forces separate template or styles file creation.',
        descriptionDetails: 'See more at https://angular.io/guide/styleguide#style-05-04',
        options: {
            type: 'array',
            items: {
                type: 'object',
            }
        },
        optionsDescription: 'Define inline template and styles lines limit.',
        optionExamples: ['[{template: 5, styles: 8}]'],
        typescriptOnly: true,
        hasFix: false
    };
    return Rule;
}(Lint.Rules.AbstractRule));
exports.Rule = Rule;
var MaxInlineDeclarationsValidator = (function (_super) {
    __extends(MaxInlineDeclarationsValidator, _super);
    function MaxInlineDeclarationsValidator(sourceFile, rule, templateLinesLimit, stylesLinesLimit) {
        var _this = _super.call(this, sourceFile, rule.getOptions()) || this;
        _this.rule = rule;
        _this.templateLinesLimit = templateLinesLimit;
        _this.stylesLinesLimit = stylesLinesLimit;
        _this.newLineRegExp = /\r\n|\r|\n/;
        return _this;
    }
    MaxInlineDeclarationsValidator.prototype.visitNgComponent = function (metadata) {
        this.validateInlineTemplate(metadata);
        this.validateInlineStyles(metadata);
        _super.prototype.visitNgComponent.call(this, metadata);
    };
    MaxInlineDeclarationsValidator.prototype.validateInlineTemplate = function (metadata) {
        if (this.hasInlineTemplate(metadata) && this.getTemplateLinesCount(metadata) > this.templateLinesLimit) {
            var templateLinesCount = this.getTemplateLinesCount(metadata);
            var msg = "Inline template lines limit exceeded. Defined limit: " + this.templateLinesLimit + " / template lines: " + templateLinesCount;
            this.addFailureAtNode(metadata.template.node, msg);
        }
    };
    MaxInlineDeclarationsValidator.prototype.hasInlineTemplate = function (metadata) {
        return !!metadata.template && !metadata.template.url && !!metadata.template.template && !!metadata.template.template.source;
    };
    MaxInlineDeclarationsValidator.prototype.getTemplateLinesCount = function (metadata) {
        return metadata.template.template.source.split(this.newLineRegExp).length;
    };
    MaxInlineDeclarationsValidator.prototype.validateInlineStyles = function (metadata) {
        if (this.hasInlineStyles(metadata) && this.getInlineStylesLinesCount(metadata) > this.stylesLinesLimit) {
            var stylesLinesCount = this.getInlineStylesLinesCount(metadata);
            var msg = "Inline styles lines limit exceeded. Defined limit: " + this.stylesLinesLimit + " / styles lines: " + stylesLinesCount;
            for (var i = 0; i < metadata.styles.length; i++) {
                this.addFailureAtNode(metadata.styles[i].node, msg);
            }
        }
    };
    MaxInlineDeclarationsValidator.prototype.hasInlineStyles = function (metadata) {
        if (!metadata.styles) {
            return false;
        }
        for (var i = 0; i < metadata.styles.length; i++) {
            var style = metadata.styles[i];
            if (!style.url && style.style && style.style.source) {
                return true;
            }
        }
        return false;
    };
    MaxInlineDeclarationsValidator.prototype.getInlineStylesLinesCount = function (metadata) {
        var result = 0;
        for (var i = 0; i < metadata.styles.length; i++) {
            if (!metadata.styles[i].url) {
                result += metadata.styles[i].style.source.split(this.newLineRegExp).length;
            }
        }
        return result;
    };
    return MaxInlineDeclarationsValidator;
}(ngWalker_1.NgWalker));
exports.MaxInlineDeclarationsValidator = MaxInlineDeclarationsValidator;
