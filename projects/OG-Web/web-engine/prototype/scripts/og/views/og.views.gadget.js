/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.gadget',
    dependencies: ['og.common', 'og.api'],
    obj: function () {
        var module = this, view, common = og.common, routes = common.routes,
            gadgets = common.gadgets, content = '.OG-gadget-container', $content = $(content);
        return view = {
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            root: function () {$content.html('No gadget was specified.');},
            grid: function (args) {
                // TODO this is a global ... remove it!
                grid = new og.analytics.Grid({selector: content});
            },
            gadgetscontainer: function (args) {
                ['center'].forEach(function (val) {
                    new og.common.gadgets.GadgetsContainer('.OG-gadgets-container-', val).add(args[val]);
                });
            },
            positions: function (args) {
                $content.html('\
                    <section class="OG-details-positions og-js-positions"></section>\
                    <section class="og-js-trades"></section>\
                ');
                gadgets.positions({
                    id: args.id, selector: '.og-js-positions', editable: false, external_links: true
                });
                if (args.trades === 'true')
                    gadgets.trades({id: args.id, selector: '.og-js-trades', editable: false, height: 150});
            },
            securities: function (args) {
                $content.html('<section></section>');
                new gadgets.SecuritiesIdentifiers({id: args.id, selector: '#gadget_content section'});
            },
            timeseries: function (args) {
                var options = {selector: '.OG-timeseries-container', datapoints_link: false};
                $content.html('<section class="' + options.selector.substring(1) + '"></section>');
                if (args.id) options.id = args.id; else options.data = og.api.common.get_cache(args.key);
                if (args.key) og.api.common.del_cache(args.key);
                if (!options.data && !options.id) return $('#gadget_content').html('There is no data to load.');
                gadgets.timeseries(options);
            },
            rules: {
                root: {route: '/frame:?', method: module.name + '.root'},
                grid: {route: '/grid/:id/frame:?', method: module.name + '.grid'},
                gadgetscontainer: {route: '/gadgetscontainer/:center/frame:?', method: module.name + '.gadgetscontainer'},
                positions: {route: '/positions/:id/trades:?/frame:?', method: module.name + '.positions'},
                securities: {route: '/securities/:id/frame:?', method: module.name + '.securities'},
                timeseries: {route: '/timeseries/id:?/key:?/frame:?', method: module.name + '.timeseries'}
            }
        }
    }
});
