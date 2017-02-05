import PerfProfiler from "./PerfProfiler";
import React from "react";
import ReactDom from "react-dom";
import client from "./client";
import InfiniteTree from "InfiniteTree";
import rowRenderer from "./renderer";
import {quoteattr} from "ITexamples/helper";
import "InfiniteTree/index.styl";
import "ITexamples/app.styl";
import "ITexamples/animation.styl";

class App extends React.Component {
    constructor(props) {
        super(props);

        this.tree = null;
        this.getStoragesByParent = this.getStoragesByParent.bind(this);
    }

    componentDidMount() {
        client({
            method: 'GET', path: '/login?login=user'
        }).then(
            response => {
                return response.entity._links.root.href;
            }).done(rootUrl => {
            client({
                method: 'GET', path: rootUrl
            }).done(
                response => {
                    let data = response.entity;
                    const result = [];

                    for (let i = 0; i < data.length; i++) {
                        result.push({
                            id: data[i].itemId,
                            name: data[i].displayName,
                            parent: null,
                            itemType: data[i].itemType,
                            itemSubType: data[i].itemSubType,
                            loadOnDemand: data[i].itemType == 'storage',
                            links: data[i].links
                        });
                    }

                    this.tree.loadData(result);
                    this.tree.selectNode(this.tree.getChildNodes()[0]);
                }
            )
        })
    }

    getStoragesByParent(parentNode) {
        if (parentNode.childrenLoaded || !parentNode.loadOnDemand)
            return;

        client({
            method: 'GET', path: parentNode.links.find(link => link.rel == 'items').href
        }).then(
            response => {
                let data = response.entity;
                const result = [];

                for (let i = 0; i < data.length; i++) {
                    result.push({
                        id: data[i].itemId,
                        name: data[i].displayName,
                        parent: parentNode,
                        itemType: data[i].itemType,
                        itemSubType: data[i].itemSubType,
                        loadOnDemand: data[i].itemType == 'storage',
                        links: data[i].links
                    });
                }

                this.tree.addChildNodes(result, 0, parentNode);
                parentNode['childrenLoaded'] = true;
                parentNode.state.open = true;
            }
        )
    }

    updatePreview(node) {
        const el = document.querySelector('[data-id="preview"]');
        if (node) {
            let o = {
                id: node.id,
                name: node.name,
                children: node.children ? node.children.length : 0,
                parent: node.parent ? node.parent.id : null,
                state: node.state
            };
            if (node.loadOnDemand !== undefined) {
                o.loadOnDemand = node.loadOnDemand;
            }
            el.innerHTML = JSON.stringify(o, null, 2).replace(/\n/g, '<br>').replace(/\s/g, '&nbsp;');
        } else {
            el.innerHTML = '';
        }
    }

    render() {
        return (
            <div>
                <InfiniteTree
                    ref={(c) => this.tree = c.tree}
                    autoOpen={true}
                    droppable={{
                        hoverClass: 'infinite-tree-drop-hover',
                        accept: function (opts) {
                            const {type, draggableTarget, droppableTarget, node} = opts;
                            return true;
                        },
                        drop: function (e, opts) {
                            const {draggableTarget, droppableTarget, node} = opts;
                            const source = e.dataTransfer.getData('text');
                            document.querySelector('[data-id="dropped-result"]').innerHTML = 'Dropped to <b>' + quoteattr(node.name) + '</b>';
                        }
                    }}
                    loadNodes={(parentNode, done) => {
                        done(null, this.getStoragesByParent(parentNode));
                    }}
                    rowRenderer={rowRenderer}
                    selectable={true} // Defaults to true
                    shouldSelectNode={(node) => { // Defaults to null
                        if (!node || (node === this.tree.getSelectedNode())) {
                            return false; // Prevent from deselecting the current node
                        }
                        return true;
                    }}
                    onDoubleClick={(event) => {
                        const target = event.target || event.srcElement; // IE8
                        console.log('onDoubleClick', target);
                    }}
                    onClick={(event) => {
                        const target = event.target || event.srcElement; // IE8
                        console.log('onClick', target);
                    }}
                    onDropNode={(node, e) => {
                        const source = e.dataTransfer.getData('text');
                        document.querySelector('[data-id="dropped-result"]').innerHTML = 'Dropped to <b>' + quoteattr(node.name) + '</b>';
                    }}
                    onContentWillUpdate={() => {
                        console.log('onContentWillUpdate');
                    }}
                    onContentDidUpdate={() => {
                        this.updatePreview(this.tree.getSelectedNode());
                    }}
                    onSelectNode={(node) => {
                        this.updatePreview(node);
                        this.getStoragesByParent(node);
                    }}
                />
            </div>
        );
    }
}

ReactDom.render(<App />, document.getElementById('react'));
ReactDom.render(<PerfProfiler/>, document.getElementById('prof'));