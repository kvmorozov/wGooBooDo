import React from "react";
import ControlLabel from "react-bootstrap/lib/ControlLabel";
import FormGroup from "react-bootstrap/lib/FormGroup";
import FormControl from "react-bootstrap/lib/FormControl";
import Form from "react-bootstrap/lib/Form";
import Col from "react-bootstrap/lib/Col";
import Button from "react-bootstrap/lib/Button";
import ButtonGroup from "react-bootstrap/lib/ButtonGroup";

class Preview extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        if (this.props == null || this.props.node == null) {
            return <Form/>
        }
        else {
            let node = this.props.node;
            let catButtons = node.categories.map(category => <Button>{category.name}</Button>);

            return (
                <Form horizontal>
                    <FormGroup controlId="formAttrsId">
                        <Col componentClass={ControlLabel} sm={2}>ID</Col>
                        <Col sm={10}>
                            <FormControl type="text"
                                         value={node.itemId}/>
                        </Col>
                    </FormGroup>

                    <FormGroup controlId="formAttrsName">
                        <Col componentClass={ControlLabel} sm={2}>Name</Col>
                        <Col sm={10}>
                            <FormControl type="text"
                                         value={node.displayName}/>
                        </Col>
                    </FormGroup>

                    <FormGroup controlId="formAttrsType">
                        <Col componentClass={ControlLabel} sm={2}>Item type</Col>
                        <Col sm={10}>
                            <FormControl type="text"
                                         value={node.itemType}/>
                        </Col>
                    </FormGroup>

                    <FormGroup controlId="formAttrsFiles">
                        <Col componentClass={ControlLabel} sm={2}>Files count</Col>
                        <Col sm={10}>
                            <FormControl type="text"
                                         value={node.filesCount}/>
                        </Col>
                    </FormGroup>
                    <ButtonGroup>
                        {catButtons}
                    </ButtonGroup>

                </Form>
            );
        }
    }
}

export default Preview;