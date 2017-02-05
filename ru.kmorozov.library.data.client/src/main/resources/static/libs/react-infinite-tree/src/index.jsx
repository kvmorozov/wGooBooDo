import React from 'react';
import ReactDOM from 'react-dom';
import ReactDOMServer from 'react-dom/server';
import InfiniteTree from './infinite-tree';
import { defaultRowRenderer } from './renderer';

const lcfirst = (str) => {
    str += '';
    return str.charAt(0).toLowerCase() + str.substr(1);
};

module.exports = class extends React.Component {
    tree = null;

    eventHandlers = {
        onClick: null,
        onDoubleClick: null,
        onClusterWillChange: null,
        onClusterDidChange: null,
        onContentWillUpdate: null,
        onContentDidUpdate: null,
        onOpenNode: null,
        onCloseNode: null,
        onSelectNode: null
    };

    componentDidMount() {
        const { children, className, ...options } = this.props;

        const el = ReactDOM.findDOMNode(this);
        options.el = el;

        const rowRenderer = options.rowRenderer || defaultRowRenderer;
        options.rowRenderer = (node, opts) => {
            let row = rowRenderer(node, opts);
            if (typeof row === 'object') {
                // Use ReactDOMServer.renderToString() to render React Component
                row = ReactDOMServer.renderToString(row);
            }
            return row;
        };

        this.tree = new InfiniteTree(options);

        Object.keys(this.eventHandlers).forEach(key => {
            if (!this.props[key]) {
                return;
            }

            const eventName = lcfirst(key.substr(2)); // e.g. onContentWillUpdate -> contentWillUpdate
            this.eventHandlers[key] = this.props[key];
            this.tree.on(eventName, this.eventHandlers[key]);
        });
    }
    componentWillUnmount() {
        Object.keys(this.eventHandlers).forEach(key => {
            if (!this.eventHandlers[key]) {
                return;
            }

            const eventName = lcfirst(key.substr(2)); // e.g. onUpdate -> update
            this.tree.removeListener(eventName, this.eventHandlers[key]);
            this.eventHandlers[key] = null;
        });

        this.tree.destroy();
        this.tree = null;
    }
    render() {
        return (
            <div {...this.props} />
        );
    }
};
