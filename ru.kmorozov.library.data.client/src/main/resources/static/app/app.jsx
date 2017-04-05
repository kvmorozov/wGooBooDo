import React from "react";
import ReactDom from "react-dom";
import client from "./restClient";
import InfiniteTree from "react-infinite-tree";
import rowRenderer from "./renderer";
import "./tree-utils/index.styl";
import "./tree-utils/app.styl";
import "./tree-utils/animation.styl";
import Nav from "react-bootstrap/lib/Nav";
import Navbar from "react-bootstrap/lib/Navbar";
import NavItem from "react-bootstrap/lib/NavItem";
import Row from "react-bootstrap/lib/Row";
import Col from "react-bootstrap/lib/Col";
import Preview from "./preview/preview";
import LoadPopup from "./loadPopup";
;

class App extends React.Component {
    constructor(props) {
        super(props);

        this.tree = null;
        this.getStoragesByParent = this.getStoragesByParent.bind(this);
        this.handleSelect = this.handleSelect.bind(this);
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
                        result.push(this.itemToNode(data[i]));
                    }

                    this.tree.loadData(result);
                    this.tree.selectNode(this.tree.getChildNodes()[0]);
                }
            )
        })
    }

    itemToNode(item, parent) {
        return {
            id: item.itemId,
            name: item.displayName,
            parent: parent,
            itemType: item.itemType,
            itemSubType: item.itemSubType,
            loadOnDemand: item.itemType == 'storage',
            links: item.links,
            filesCount: item.filesCount
        };
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
                    if (data[i].filesCount > 0)
                        result.push(this.itemToNode(data[i], parentNode));
                }

                this.tree.addChildNodes(result, 0, parentNode);
                parentNode['childrenLoaded'] = true;
                parentNode.state.open = true;
            }
        )
    }

    updatePreview(node) {
        if (node != null)
            client({
                method: 'GET', path: node.links.find(link => link.rel == 'self').href
            }).then(
                response => {
                    this.refs.preview.update(response.entity);
                }
            );
    }

    handleSelect(selectedKey) {
        this.refs.loadPopup.open();
    }

    render() {
        return (
            <div className="row-fluid">
                <Navbar>
                    <Navbar.Header>
                        <Navbar.Brand>Library Client</Navbar.Brand>
                    </Navbar.Header>
                    <Navbar.Toggle/>
                    <Navbar.Collapse>
                        <Nav navbar onSelect={this.handleSelect}>
                            <NavItem eventKey={1}>Update library</NavItem>
                            <NavItem eventKey={2}>Options</NavItem>
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>
                <Row>
                    <Col xs={2} md={6}>
                        <InfiniteTree
                            ref={(c) => this.tree = c.tree}
                            autoOpen={true}
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
                            }}
                            onClick={(event) => {
                                const target = event.target || event.srcElement; // IE8
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
                    <Col xs={2} md={6}>
                        <Preview ref="preview"/>
                    </Col>
                </Row>

                <LoadPopup ref="loadPopup"/>
            </div>
        );
    }
}

ReactDom.render(<App />, document.getElementById('react'));