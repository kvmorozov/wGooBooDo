import React from "react";
import Form from "react-bootstrap/lib/Form";
import Button from "react-bootstrap/lib/Button";
import ButtonGroup from "react-bootstrap/lib/ButtonGroup";
import BaseAttrs from "./baseAttrs";
import client from "./../restClient";
import BooksList from "./booksList";

class Preview extends React.Component {

    constructor(props) {
        super(props);

        this.update = this.update.bind(this);
    }

    update(node) {
        this.setState({node: node});
        if (node._links != null)
            client({
                method: 'GET', path: node._links['books'].href
            }).then(
                response => {
                    this.refs.booksList.update(response.entity);
                }
            );
    }

    render() {
        if (this.state == null || this.state.node == null) {
            return <Form/>
        }
        else {
            let node = this.state.node;
            let catButtons = node.categories.map(category => <Button key={category.name}>{category.name}</Button>);

            return (
                <Form horizontal>
                    <BaseAttrs node={node}/>
                    <ButtonGroup>
                        {catButtons}
                    </ButtonGroup>
                    <BooksList ref="booksList"/>
                </Form>
            );
        }
    }
}

export default Preview;