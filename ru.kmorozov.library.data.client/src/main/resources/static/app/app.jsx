import React from "react";
import ReactDom from "react-dom";
import client from "./client";
import InfiniteTree from "react-infinite-tree";
import rowRenderer from "./renderer";
import {quoteattr} from "./tree-utils/helper";
import "./tree-utils/index.styl";
import "./tree-utils/app.styl";
import "./tree-utils/animation.styl";
import Nav from "react-bootstrap/lib/Nav";
import Navbar from "react-bootstrap/lib/Navbar";
import NavItem from "react-bootstrap/lib/NavItem";
import Grid from "react-bootstrap/lib/Grid";
import Row from "react-bootstrap/lib/Row";
import Col from "react-bootstrap/lib/Col";
import Preview from "./preview";
import "../bootstrap.min.css";

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
                            links: data[i].links,
                            filesCount: data[i].filesCount
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
                        links: data[i].links,
                        filesCount: data[i].filesCount
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
        ReactDom.render(<Preview node={node}/>, el);
    }

    render() {
        return (
            <div>
                <Navbar>
                    <Navbar.Header>
                        <Navbar.Brand>Library Client</Navbar.Brand>
                    </Navbar.Header>
                    <Navbar.Toggle/>
                    <Navbar.Collapse>
                        <Nav navbar>
                            <NavItem>Menu1</NavItem>
                            <NavItem>Menu2</NavItem>
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>
                <Grid>
                    <Row>
                        <Col>
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
                        </Col>
                        <Col>
                            <div className="preview" data-id="preview"/>
                        </Col>
                    </Row>
                </Grid>
            </div>
        );
    }
}

ReactDom.render(<App />, document.getElementById('react'));